package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.TrendingSearch;
import ml.docilealligator.infinityforreddit.adapters.TrendingSearchRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityTrendingBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.post.ParsePost;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TrendingActivity extends BaseActivity {
    private static final String TRENDING_SEARCHES_STATE = "TSS";

    private ActivityTrendingBinding binding;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mAccessToken;
    private boolean isRefreshing = false;
    private ArrayList<TrendingSearch> trendingSearches;
    private TrendingSearchRecyclerViewAdapter adapter;
    private RequestManager mGlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        binding = ActivityTrendingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appBarLayout);
        }

        Window window = getWindow();
        if (isImmersiveInterface()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
            } else {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
            adjustToolbar(binding.toolbar);

            int navBarResourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerView.setPadding(0, 0, 0, binding.recyclerView.getPaddingBottom() + getResources().getDimensionPixelSize(navBarResourceId));
            }
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbar);

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        mGlide = Glide.with(this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int windowWidth = displayMetrics.widthPixels;

        String dataSavingModeString = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        boolean dataSavingMode = false;
        if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
            dataSavingMode = true;
        } else if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            int networkType = Utils.getConnectedNetwork(this);
            dataSavingMode = networkType == Utils.NETWORK_TYPE_CELLULAR;
        }
        boolean disableImagePreview = mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_IMAGE_PREVIEW, false);
        adapter = new TrendingSearchRecyclerViewAdapter(this, mCustomThemeWrapper, windowWidth,
                dataSavingMode, disableImagePreview, new TrendingSearchRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onClick(TrendingSearch trendingSearch) {
                Intent intent = new Intent(TrendingActivity.this, SearchResultActivity.class);
                intent.putExtra(SearchResultActivity.EXTRA_QUERY, trendingSearch.queryString);
                intent.putExtra(SearchResultActivity.EXTRA_TRENDING_SOURCE, "trending");
                startActivity(intent);
            }
        });
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefreshLayout.setEnabled(mSharedPreferences.getBoolean(SharedPreferencesUtils.PULL_TO_REFRESH, true));
        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchTrendingSearches);

        binding.fetchTrendingSearchLinearLayout.setOnClickListener(view -> fetchTrendingSearches());

        if (savedInstanceState != null) {
            trendingSearches = savedInstanceState.getParcelableArrayList(TRENDING_SEARCHES_STATE);
        }
        if (trendingSearches != null) {
            adapter.setTrendingSearches(trendingSearches);
        } else {
            fetchTrendingSearches();
        }
    }

    private void fetchTrendingSearches() {
        if (isRefreshing) {
            return;
        }
        isRefreshing = true;

        binding.fetchTrendingSearchLinearLayout.setVisibility(View.GONE);
        Glide.with(this).clear(binding.fetchTrendingSearchImageView);
        binding.swipeRefreshLayout.setRefreshing(true);
        trendingSearches = null;
        adapter.setTrendingSearches(null);
        Handler handler = new Handler();
        Call<String> trendingCall;
        if (mAccessToken == null) {
            trendingCall = mRetrofit.create(RedditAPI.class).getTrendingSearches();
        } else {
            trendingCall = mOauthRetrofit.create(RedditAPI.class).getTrendingSearchesOauth(APIUtils.getOAuthHeader(mAccessToken));
        }
        trendingCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    mExecutor.execute(() -> {
                        try {
                            JSONArray trendingSearchesArray = new JSONObject(response.body()).getJSONArray(JSONUtils.TRENDING_SEARCHES_KEY);
                            ArrayList<TrendingSearch> trendingSearchList = new ArrayList<>();
                            for (int i = 0; i < trendingSearchesArray.length(); i++) {
                                try {
                                    JSONObject trendingSearchObject = trendingSearchesArray.getJSONObject(i);
                                    String queryString = trendingSearchObject.getString(JSONUtils.QUERY_STRING_KEY);
                                    String displayString = trendingSearchObject.getString(JSONUtils.DISPLAY_STRING_KEY);
                                    JSONArray childrenWithOnlyOneChild = trendingSearchObject
                                            .getJSONObject(JSONUtils.RESULTS_KEY)
                                            .getJSONObject(JSONUtils.DATA_KEY)
                                            .getJSONArray(JSONUtils.CHILDREN_KEY);
                                    if (childrenWithOnlyOneChild.length() > 0) {
                                        Post post = ParsePost.parseBasicData(childrenWithOnlyOneChild.getJSONObject(0)
                                                .getJSONObject(JSONUtils.DATA_KEY));

                                        trendingSearchList.add(new TrendingSearch(queryString, displayString,
                                                post.getTitle(), post.getPreviews()));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            handler.post(() -> {
                                trendingSearches = trendingSearchList;
                                binding.swipeRefreshLayout.setRefreshing(false);
                                adapter.setTrendingSearches(trendingSearches);
                                isRefreshing = false;
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            handler.post(() -> {
                                binding.swipeRefreshLayout.setRefreshing(false);
                                showErrorView(R.string.error_parse_trending_search);
                                isRefreshing = false;
                            });
                        }
                    });
                } else {
                    handler.post(() -> {
                        binding.swipeRefreshLayout.setRefreshing(false);
                        showErrorView(R.string.error_fetch_trending_search);
                        isRefreshing = false;
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                handler.post(() -> {
                    binding.swipeRefreshLayout.setRefreshing(false);
                    showErrorView(R.string.error_fetch_trending_search);
                    isRefreshing = false;
                });
            }
        });
    }

    private void showErrorView(int stringId) {
        binding.fetchTrendingSearchLinearLayout.setVisibility(View.VISIBLE);
        mGlide.load(R.drawable.error_image).into(binding.fetchTrendingSearchImageView);
        binding.fetchTrendingSearchTextView.setText(stringId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trending_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_refresh_trending_activity) {
            fetchTrendingSearches();
            return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TRENDING_SEARCHES_STATE, trendingSearches);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(
                binding.appBarLayout, binding.collapsingToolbarLayout, binding.toolbar);
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        binding.fetchTrendingSearchTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}