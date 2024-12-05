package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.lifecycle.ViewModelProvider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.SearchActivityRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.GqlAPI;
import ml.docilealligator.infinityforreddit.apis.GqlRequestBody;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivitySearchBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.recentsearchquery.DeleteRecentSearchQuery;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQuery;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQueryViewModel;
import ml.docilealligator.infinityforreddit.subreddit.ParseSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchActivity extends BaseActivity {
    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_SUBREDDIT_IS_USER = "ESIU";
    public static final String EXTRA_SEARCH_ONLY_SUBREDDITS = "ESOS";
    public static final String EXTRA_SEARCH_ONLY_USERS = "ESOU";
    public static final String EXTRA_RETURN_SUBREDDIT_NAME = "ERSN";
    public static final String EXTRA_RETURN_SUBREDDIT_ICON_URL = "ERSIU";
    public static final String RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES = "RESSN";
    public static final String RETURN_EXTRA_SELECTED_USERNAMES = "RESU";
    public static final String EXTRA_RETURN_USER_NAME = "ERUN";
    public static final String EXTRA_RETURN_USER_ICON_URL = "ERUIU";
    public static final String EXTRA_IS_MULTI_SELECTION = "EIMS";
    public static final int SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE = 101;

    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;
    private static final int USER_SEARCH_REQUEST_CODE = 2;

    private ActivitySearchBinding binding;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("gql")
    Retrofit mGQLRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    @Named("anonymous_account")
    SharedPreferences mAnonymousAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String mAccountName;
    private String mAccessToken;
    private String query;
    private String subredditName;
    private boolean subredditIsUser;
    private boolean searchOnlySubreddits;
    private boolean searchOnlyUsers;
    private SearchActivityRecyclerViewAdapter adapter;
    private SubredditAutocompleteRecyclerViewAdapter subredditAutocompleteRecyclerViewAdapter;
    private Call<String> subredditAutocompleteCall;
    RecentSearchQueryViewModel mRecentSearchQueryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        setSupportActionBar(binding.toolbar);

        binding.clearSearchEditView.setVisibility(View.GONE);

        searchOnlySubreddits = getIntent().getBooleanExtra(EXTRA_SEARCH_ONLY_SUBREDDITS, false);
        searchOnlyUsers = getIntent().getBooleanExtra(EXTRA_SEARCH_ONLY_USERS, false);

        if (searchOnlySubreddits) {
            binding.searchEditText.setHint(getText(R.string.search_only_subreddits_hint));
        } else if (searchOnlyUsers) {
            binding.searchEditText.setHint(getText(R.string.search_only_users_hint));
        }

        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);
        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        boolean nsfw = mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false);

        subredditAutocompleteRecyclerViewAdapter = new SubredditAutocompleteRecyclerViewAdapter(this,
                mCustomThemeWrapper, subredditData -> {
            if (searchOnlySubreddits) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    ArrayList<String> subredditNameList = new ArrayList<>();
                    subredditNameList.add(subredditData.getName());
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, subredditNameList);
                } else {
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, subredditData.getName());
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, subredditData.getIconUrl());
                }
                setResult(Activity.RESULT_OK, returnIntent);
            } else {
                Intent intent = new Intent(SearchActivity.this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
                startActivity(intent);
            }
            finish();
        });

        if (mAccessToken == null) {
            binding.searchEditText.setImeOptions(binding.searchEditText.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    if (subredditAutocompleteCall != null) {
                        subredditAutocompleteCall.cancel();
                    }

                    if (mAccessToken == null) {
                        mAccessToken = mAnonymousAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
                    }
                    if (mAccessToken == null) return;

                    subredditAutocompleteCall = mGQLRetrofit.create(GqlAPI.class).subredditAutocomplete(APIUtils.getOAuthHeader(mAccessToken),
                            GqlRequestBody.subredditAutocompleteBody(s.toString(), nsfw));
                    subredditAutocompleteCall.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful()) {
                                ParseSubredditData.parseSubredditListingData(response.body(), nsfw, new ParseSubredditData.ParseSubredditListingDataListener() {
                                    @Override
                                    public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                                        subredditAutocompleteRecyclerViewAdapter.setSubreddits(subredditData);
                                        binding.recyclerView.setAdapter(subredditAutocompleteRecyclerViewAdapter);
                                    }

                                    @Override
                                    public void onParseSubredditListingDataFail() {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                        }
                    });
                    binding.clearSearchEditView.setVisibility(View.VISIBLE);
                } else {
                    binding.clearSearchEditView.setVisibility(View.GONE);
                }
            }
        });

        binding.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (!binding.searchEditText.getText().toString().isEmpty()) {
                    search(binding.searchEditText.getText().toString());
                    return true;
                }
            }
            return false;
        });

        binding.clearSearchEditView.setOnClickListener(view -> {
            binding.searchEditText.getText().clear();
        });

        binding.linkHandlerImageView.setOnClickListener(view -> {
            if (!binding.searchEditText.getText().toString().equals("")) {
                Intent intent = new Intent(this, LinkResolverActivity.class);
                intent.setData(Uri.parse(binding.searchEditText.getText().toString()));
                startActivity(intent);
                finish();
            }
        });

        if (savedInstanceState != null) {
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);

            if (subredditName == null) {
                binding.subredditNameTextView.setText(R.string.all_subreddits);
            } else {
                binding.subredditNameTextView.setText(subredditName);
            }
        } else {
            query = getIntent().getStringExtra(EXTRA_QUERY);
        }
        bindView();

        if (searchOnlySubreddits || searchOnlyUsers) {
            binding.subredditNameRelativeLayout.setVisibility(View.GONE);
        } else {
            binding.subredditNameRelativeLayout.setOnClickListener(view -> {
                Intent intent = new Intent(this, SubredditSelectionActivity.class);
                intent.putExtra(SubredditSelectionActivity.EXTRA_EXTRA_CLEAR_SELECTION, true);
                startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
            });
        }

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SUBREDDIT_NAME)) {
            subredditName = intent.getStringExtra(EXTRA_SUBREDDIT_NAME);
            binding.subredditNameTextView.setText(subredditName);
            subredditIsUser = intent.getBooleanExtra(EXTRA_SUBREDDIT_IS_USER, false);
        }
    }

    private void bindView() {
        adapter = new SearchActivityRecyclerViewAdapter(this, mCustomThemeWrapper, new SearchActivityRecyclerViewAdapter.ItemOnClickListener() {
            @Override
            public void onClick(String query) {
                search(query);
            }

            @Override
            public void onDelete(RecentSearchQuery recentSearchQuery) {
                DeleteRecentSearchQuery.deleteRecentSearchQueryListener(mRedditDataRoomDatabase, recentSearchQuery, () -> {
                });
            }
        });
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.recyclerView.setNestedScrollingEnabled(false);
        binding.recyclerView.setAdapter(adapter);

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SEARCH_HISTORY, true)) {
            mRecentSearchQueryViewModel = new ViewModelProvider(this,
                    new RecentSearchQueryViewModel.Factory(mRedditDataRoomDatabase, mAccountName))
                    .get(RecentSearchQueryViewModel.class);

            mRecentSearchQueryViewModel.getAllRecentSearchQueries().observe(this, recentSearchQueries -> {
                if (recentSearchQueries != null && !recentSearchQueries.isEmpty()) {
                    binding.divider.setVisibility(View.VISIBLE);
                } else {
                    binding.divider.setVisibility(View.GONE);
                }
                adapter.setRecentSearchQueries(recentSearchQueries);
            });
        }
    }

    private void search(String query) {
        if (query.equalsIgnoreCase("suicide") && mSharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_SUICIDE_PREVENTION_ACTIVITY, true)) {
            Intent intent = new Intent(this, SuicidePreventionActivity.class);
            intent.putExtra(SuicidePreventionActivity.EXTRA_QUERY, query);
            startActivityForResult(intent, SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE);
        } else {
            openSearchResult(query);
        }
    }

    private void openSearchResult(String query) {
        if (searchOnlySubreddits) {
            Intent intent = new Intent(SearchActivity.this, SearchSubredditsResultActivity.class);
            intent.putExtra(SearchSubredditsResultActivity.EXTRA_QUERY, query);
            intent.putExtra(SearchSubredditsResultActivity.EXTRA_IS_MULTI_SELECTION, getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false));
            startActivityForResult(intent, SUBREDDIT_SEARCH_REQUEST_CODE);
        } else if (searchOnlyUsers) {
            Intent intent = new Intent(this, SearchUsersResultActivity.class);
            intent.putExtra(SearchUsersResultActivity.EXTRA_QUERY, query);
            intent.putExtra(SearchUsersResultActivity.EXTRA_IS_MULTI_SELECTION, getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false));
            startActivityForResult(intent, USER_SEARCH_REQUEST_CODE);
        } else {
            Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
            intent.putExtra(SearchResultActivity.EXTRA_QUERY, query);
            if (subredditName != null) {
                if (subredditIsUser) {
                    intent.putExtra(SearchResultActivity.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
                } else {
                    intent.putExtra(SearchResultActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                }
            }
            startActivity(intent);
            finish();
        }
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
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
                binding.appBarLayout, null, binding.toolbar);
        int toolbarPrimaryTextAndIconColorColor = mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor();
        binding.searchEditText.setTextColor(toolbarPrimaryTextAndIconColorColor);
        binding.searchEditText.setHintTextColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        binding.clearSearchEditView.setColorFilter(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        binding.linkHandlerImageView.setColorFilter(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        int colorAccent = mCustomThemeWrapper.getColorAccent();
        binding.searchInTextView.setTextColor(colorAccent);
        binding.subredditNameTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        binding.divider.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.searchEditText.requestFocus();

        if (query != null) {
            binding.searchEditText.setText(query);
            binding.searchEditText.setSelection(query.length());
            query = null;
        }

        Utils.showKeyboard(this, new Handler(), binding.searchEditText);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideKeyboard(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
                subredditName = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                subredditIsUser = data.getBooleanExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER, false);

                if (subredditName == null) {
                    binding.subredditNameTextView.setText(R.string.all_subreddits);
                } else {
                    binding.subredditNameTextView.setText(subredditName);
                }
            } else if (requestCode == SUBREDDIT_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, data.getStringArrayListExtra(SearchSubredditsResultActivity.RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES));
                } else {
                    String name = data.getStringExtra(SearchSubredditsResultActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                    String iconUrl = data.getStringExtra(SearchSubredditsResultActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, name);
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, iconUrl);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (requestCode == USER_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_USERNAMES, data.getStringArrayListExtra(SearchUsersResultActivity.RETURN_EXTRA_SELECTED_USERNAMES));
                } else {
                    String username = data.getStringExtra(SearchUsersResultActivity.EXTRA_RETURN_USER_NAME);
                    String iconUrl = data.getStringExtra(SearchUsersResultActivity.EXTRA_RETURN_USER_ICON_URL);
                    returnIntent.putExtra(EXTRA_RETURN_USER_NAME, username);
                    returnIntent.putExtra(EXTRA_RETURN_USER_ICON_URL, iconUrl);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (requestCode == SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE) {
                openSearchResult(data.getStringExtra(SuicidePreventionActivity.EXTRA_RETURN_QUERY));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SUBREDDIT_NAME_STATE, subredditName);
        outState.putBoolean(SUBREDDIT_IS_USER_STATE, subredditIsUser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}
