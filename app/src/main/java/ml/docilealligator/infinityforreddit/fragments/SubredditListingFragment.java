package ml.docilealligator.infinityforreddit.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.SearchSubredditsResultActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.adapters.SubredditListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentSubredditListingBinding;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditListingViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubredditListingFragment extends Fragment implements FragmentCommunicator {
    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_IS_GETTING_SUBREDDIT_INFO = "EIGSI";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_IS_MULTI_SELECTION = "EIMS";

    private FragmentSubredditListingBinding binding;
    SubredditListingViewModel mSubredditListingViewModel;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
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
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private SubredditListingRecyclerViewAdapter mAdapter;
    private BaseActivity mActivity;
    private SortType sortType;

    public SubredditListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSubredditListingBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        applyTheme();

        Resources resources = getResources();

        if ((mActivity != null && ((BaseActivity) mActivity).isImmersiveInterface())) {
            binding.recyclerView.setPadding(0, 0, 0, ((BaseActivity) mActivity).getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        mLinearLayoutManager = new LinearLayoutManagerBugFixed(getActivity());
        binding.recyclerView.setLayoutManager(mLinearLayoutManager);

        String query = getArguments().getString(EXTRA_QUERY);
        boolean isGettingSubredditInfo = getArguments().getBoolean(EXTRA_IS_GETTING_SUBREDDIT_INFO);
        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SEARCH_SUBREDDIT, SortType.Type.RELEVANCE.value);
        sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()));
        boolean nsfw = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);

        mAdapter = new SubredditListingRecyclerViewAdapter(mActivity, mExecutor, mGQLRetrofit, mRetrofit,
                mCustomThemeWrapper, accessToken, accountName,
                mRedditDataRoomDatabase, getArguments().getBoolean(EXTRA_IS_MULTI_SELECTION, false),
                new SubredditListingRecyclerViewAdapter.Callback() {
                    @Override
                    public void retryLoadingMore() {
                        mSubredditListingViewModel.retryLoadingMore();
                    }

                    @Override
                    public void subredditSelected(String subredditName, String iconUrl) {
                        if (isGettingSubredditInfo) {
                            ((SearchSubredditsResultActivity) mActivity).getSelectedSubreddit(subredditName, iconUrl);
                        } else {
                            Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditName);
                            mActivity.startActivity(intent);
                        }
                    }
                });

        binding.recyclerView.setAdapter(mAdapter);

        if (mActivity instanceof RecyclerViewContentScrollingInterface) {
            binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        ((RecyclerViewContentScrollingInterface) mActivity).contentScrollDown();
                    } else if (dy < 0) {
                        ((RecyclerViewContentScrollingInterface) mActivity).contentScrollUp();
                    }
                }
            });
        }

        SubredditListingViewModel.Factory factory = new SubredditListingViewModel.Factory(
                mGQLRetrofit, query, sortType, accessToken, nsfw);
        mSubredditListingViewModel = new ViewModelProvider(this, factory).get(SubredditListingViewModel.class);
        mSubredditListingViewModel.getSubreddits().observe(getViewLifecycleOwner(), subredditData -> mAdapter.submitList(subredditData));

        SwipeRefreshLayout swipeRefreshLayout = binding.swipeRefreshLayout;
        LinearLayout listingInfoLinearLayout = binding.listingInfoLinearLayout;
        mSubredditListingViewModel.hasSubredditLiveData().observe(getViewLifecycleOwner(), hasSubreddit -> {
            swipeRefreshLayout.setRefreshing(false);
            if (hasSubreddit) {
                listingInfoLinearLayout.setVisibility(View.GONE);
            } else {
                listingInfoLinearLayout.setOnClickListener(null);
                showErrorView(R.string.no_subreddits);
            }
        });

        mSubredditListingViewModel.getInitialLoadingState().observe(getViewLifecycleOwner(), networkState -> {
            if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                swipeRefreshLayout.setRefreshing(false);
            } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                swipeRefreshLayout.setRefreshing(false);
                listingInfoLinearLayout.setOnClickListener(view -> refresh());
                showErrorView(R.string.search_subreddits_error);
            } else {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        mSubredditListingViewModel.getPaginationNetworkState().observe(getViewLifecycleOwner(), networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> mSubredditListingViewModel.refresh());

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    private void showErrorView(int stringResId) {
        if (getActivity() != null && isAdded()) {
            binding.swipeRefreshLayout.setRefreshing(false);
            binding.listingInfoLinearLayout.setVisibility(View.VISIBLE);
            binding.listingInfoTextView.setText(stringResId);
            Glide.with(this).load(R.drawable.error_image).into(binding.listingInfoImageView);
        }
    }

    public void changeSortType(SortType sortType) {
        mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SEARCH_SUBREDDIT, sortType.getType().name()).apply();
        mSubredditListingViewModel.changeSortType(sortType);
        this.sortType = sortType;
    }

    @Override
    public void refresh() {
        binding.listingInfoLinearLayout.setVisibility(View.GONE);
        mSubredditListingViewModel.refresh();
        mAdapter.setNetworkState(null);
    }

    @Override
    public void applyTheme() {
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        binding.listingInfoTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            binding.listingInfoTextView.setTypeface(mActivity.contentTypeface);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public SortType getSortType() {
        return sortType;
    }

    public ArrayList<String> getSelectedSubredditNames() {
        if (mSubredditListingViewModel != null) {
            List<SubredditData> allSubreddits = mSubredditListingViewModel.getSubreddits().getValue();
            if (allSubreddits == null) {
                return null;
            }

            ArrayList<String> selectedSubreddits = new ArrayList<>();
            for (SubredditData s : allSubreddits) {
                if (s.isSelected()) {
                    selectedSubreddits.add(s.getName());
                }
            }
            return selectedSubreddits;
        }

        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
