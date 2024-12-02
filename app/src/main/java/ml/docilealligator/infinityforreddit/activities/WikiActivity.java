package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.recycler.MarkwonAdapter;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockInterface;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityWikiBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WikiActivity extends BaseActivity {
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_WIKI_PATH = "EWP";
    private static final String WIKI_MARKDOWN_STATE = "WMS";

    private ActivityWikiBinding binding;

    @Inject
    @Named("no_oauth")
    Retrofit retrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String wikiMarkdown;
    private Markwon markwon;
    private MarkwonAdapter markwonAdapter;
    private boolean isRefreshing = false;
    private RequestManager mGlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        binding = ActivityWikiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
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
            RecyclerView recyclerView = binding.markdownRecyclerView;
            recyclerView.setPadding(recyclerView.getPaddingLeft(), 0, recyclerView.getPaddingRight(), getNavBarHeight());
        }

        mGlide = Glide.with(this);

        binding.swipeRefreshLayout.setEnabled(mSharedPreferences.getBoolean(SharedPreferencesUtils.PULL_TO_REFRESH, true));
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadWiki);

        int markdownColor = mCustomThemeWrapper.getPrimaryTextColor();
        int spoilerBackgroundColor = markdownColor | 0xFF000000;
        int linkColor = mCustomThemeWrapper.getLinkColor();
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (contentTypeface != null) {
                    textView.setTypeface(contentTypeface);
                }
                textView.setTextColor(markdownColor);
            }

            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(WikiActivity.this, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    startActivity(intent);
                });
            }

            @Override
            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                builder.linkColor(linkColor);
            }
        };
        BetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener = (textView, url) -> {
            UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url);
            urlMenuBottomSheetFragment.show(getSupportFragmentManager(), null);
            return true;
        };
        markwon = MarkdownUtils.createFullRedditMarkwon(this,
                miscPlugin, markdownColor, spoilerBackgroundColor, onLinkLongClickListener);

        markwonAdapter = MarkdownUtils.createTablesAdapter();
        LinearLayoutManagerBugFixed linearLayoutManager = new SwipeLockLinearLayoutManager(this, new SwipeLockInterface() {
            @Override
            public void lockSwipe() {
                if (mSliderPanel != null) {
                    mSliderPanel.lock();
                }
            }

            @Override
            public void unlockSwipe() {
                if (mSliderPanel != null) {
                    mSliderPanel.unlock();
                }
            }
        });
        binding.markdownRecyclerView.setLayoutManager(linearLayoutManager);
        binding.markdownRecyclerView.setAdapter(markwonAdapter);

        if (savedInstanceState != null) {
            wikiMarkdown = savedInstanceState.getString(WIKI_MARKDOWN_STATE);
        }

        if (wikiMarkdown == null) {
            loadWiki();
        } else {
            markwonAdapter.setMarkdown(markwon, wikiMarkdown);
            // noinspection NotifyDataSetChanged
            markwonAdapter.notifyDataSetChanged();
        }
    }

    private void loadWiki() {
        if (isRefreshing) {
            return;
        }
        isRefreshing = true;

        binding.swipeRefreshLayout.setRefreshing(true);

        Glide.with(this).clear(binding.fetchWikiImageView);
        binding.fetchWikiLinearLayout.setVisibility(View.GONE);

        retrofit.create(RedditAPI.class).getWikiPage(getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME), getIntent().getStringExtra(EXTRA_WIKI_PATH)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        String markdown = new JSONObject(response.body())
                                .getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.CONTENT_MD_KEY);
                        markwonAdapter.setMarkdown(markwon, Utils.modifyMarkdown(markdown));
                        // noinspection NotifyDataSetChanged
                        markwonAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showErrorView(R.string.error_loading_wiki);
                    }
                } else {
                    if (response.code() == 404 || response.code() == 403) {
                        showErrorView(R.string.no_wiki);
                    } else {
                        showErrorView(R.string.error_loading_wiki);
                    }
                }
                isRefreshing = false;
                binding.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showErrorView(R.string.error_loading_wiki);
                isRefreshing = false;
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showErrorView(int stringResId) {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.fetchWikiLinearLayout.setVisibility(View.VISIBLE);
        binding.fetchWikiTextView.setText(stringResId);
        mGlide.load(R.drawable.error_image).into(binding.fetchWikiImageView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(WIKI_MARKDOWN_STATE, wikiMarkdown);
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
        binding.fetchWikiTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (typeface != null) {
            binding.fetchWikiTextView.setTypeface(typeface);
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }
}