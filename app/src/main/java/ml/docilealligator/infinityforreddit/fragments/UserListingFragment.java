package ml.docilealligator.infinityforreddit.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import ml.docilealligator.infinityforreddit.activities.SearchUsersResultActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.adapters.UserListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentUserListingBinding;
import ml.docilealligator.infinityforreddit.user.UserData;
import ml.docilealligator.infinityforreddit.user.UserListingViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserListingFragment extends Fragment implements FragmentCommunicator {
    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_IS_GETTING_USER_INFO = "EIGUI";
    public static final String EXTRA_IS_MULTI_SELECTION = "EIMS";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    private FragmentUserListingBinding binding;
    UserListingViewModel mUserListingViewModel;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
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
    private String mQuery;
    private UserListingRecyclerViewAdapter mAdapter;
    private BaseActivity mActivity;
    private SortType sortType;

    public UserListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserListingBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        applyTheme();

        Resources resources = getResources();

        if ((mActivity instanceof BaseActivity && ((BaseActivity) mActivity).isImmersiveInterface())) {
            binding.recyclerView.setPadding(0, 0, 0, ((BaseActivity) mActivity).getNavBarHeight());
        } else if (mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        binding.recyclerView.setLayoutManager(mLinearLayoutManager);

        mQuery = getArguments().getString(EXTRA_QUERY);
        boolean isGettingUserInfo = getArguments().getBoolean(EXTRA_IS_GETTING_USER_INFO);
        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);
        String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SEARCH_USER, SortType.Type.RELEVANCE.value);
        sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()));
        boolean nsfw = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);

        mAdapter = new UserListingRecyclerViewAdapter(mActivity, mExecutor, mOauthRetrofit, mRetrofit,
                mCustomThemeWrapper, accessToken, accountName, mRedditDataRoomDatabase,
                getArguments().getBoolean(EXTRA_IS_MULTI_SELECTION, false),
                new UserListingRecyclerViewAdapter.Callback() {
                    @Override
                    public void retryLoadingMore() {
                        mUserListingViewModel.retryLoadingMore();
                    }

                    @Override
                    public void userSelected(String username, String iconUrl) {
                        if (isGettingUserInfo) {
                            ((SearchUsersResultActivity) mActivity).getSelectedUser(username, iconUrl);
                        } else {
                            Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, username);
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

        UserListingViewModel.Factory factory = new UserListingViewModel.Factory(mRetrofit, mQuery,
                sortType, nsfw);
        mUserListingViewModel = new ViewModelProvider(this, factory).get(UserListingViewModel.class);
        mUserListingViewModel.getUsers().observe(getViewLifecycleOwner(), UserData -> mAdapter.submitList(UserData));

        SwipeRefreshLayout swipeRefreshLayout = binding.swipeRefreshLayout;
        mUserListingViewModel.hasUser().observe(getViewLifecycleOwner(), hasUser -> {
            swipeRefreshLayout.setRefreshing(false);
            if (hasUser) {
                binding.listingInfoLinearLayout.setVisibility(View.GONE);
            } else {
                binding.listingInfoLinearLayout.setOnClickListener(view -> {
                    //Do nothing
                });
                showErrorView(R.string.no_users);
            }
        });

        mUserListingViewModel.getInitialLoadingState().observe(getViewLifecycleOwner(), networkState -> {
            if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                swipeRefreshLayout.setRefreshing(false);
            } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                swipeRefreshLayout.setRefreshing(false);
                binding.listingInfoLinearLayout.setOnClickListener(view -> refresh());
                showErrorView(R.string.search_users_error);
            } else {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        mUserListingViewModel.getPaginationNetworkState().observe(getViewLifecycleOwner(), networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> mUserListingViewModel.refresh());

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
        mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SEARCH_USER, sortType.getType().name()).apply();
        mUserListingViewModel.changeSortType(sortType);
        this.sortType = sortType;
    }

    @Override
    public void refresh() {
        binding.listingInfoLinearLayout.setVisibility(View.GONE);
        mUserListingViewModel.refresh();
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

    public ArrayList<String> getSelectedUsernames() {
        if (mUserListingViewModel != null) {
            List<UserData> allUsers = mUserListingViewModel.getUsers().getValue();
            if (allUsers != null) {
                ArrayList<String> selectedUsernames = new ArrayList<>();
                for (UserData u : allUsers) {
                    if (u.isSelected()) {
                        selectedUsernames.add(u.getName());
                    }
                }
                return selectedUsernames;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
