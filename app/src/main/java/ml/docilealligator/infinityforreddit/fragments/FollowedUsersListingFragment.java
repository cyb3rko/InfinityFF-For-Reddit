package ml.docilealligator.infinityforreddit.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.adapters.FollowedUsersRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentFollowedUsersListingBinding;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class FollowedUsersListingFragment extends Fragment implements FragmentCommunicator {
    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    
    private FragmentFollowedUsersListingBinding binding;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    SubscribedUserViewModel mSubscribedUserViewModel;
    private BaseActivity mActivity;
    private RequestManager mGlide;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;

    public FollowedUsersListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFollowedUsersListingBinding.inflate(inflater, container, false);
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

        mGlide = Glide.with(this);

        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        if (accessToken == null) {
            binding.swipeRefreshLayout.setEnabled(false);
        }
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        binding.recyclerView.setLayoutManager(mLinearLayoutManager);
        FollowedUsersRecyclerViewAdapter adapter = new FollowedUsersRecyclerViewAdapter(mActivity,
                mExecutor, mOauthRetrofit, mRedditDataRoomDatabase, mCustomThemeWrapper, accessToken);
        binding.recyclerView.setAdapter(adapter);
        new FastScrollerBuilder(binding.recyclerView).useMd2Style().build();

        mSubscribedUserViewModel = new ViewModelProvider(this,
                new SubscribedUserViewModel.Factory(mActivity.getApplication(), mRedditDataRoomDatabase, getArguments().getString(EXTRA_ACCOUNT_NAME)))
                .get(SubscribedUserViewModel.class);

        mSubscribedUserViewModel.getAllSubscribedUsers().observe(getViewLifecycleOwner(), subscribedUserData -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            if (subscribedUserData == null || subscribedUserData.size() == 0) {
                binding.recyclerView.setVisibility(View.GONE);
                binding.noSubscriptionsLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(binding.noSubscriptionsImageView);
            } else {
                binding.noSubscriptionsLinearLayout.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(binding.noSubscriptionsImageView);
            }
            adapter.setSubscribedUsers(subscribedUserData);
        });

        mSubscribedUserViewModel.getAllFavoriteSubscribedUsers().observe(getViewLifecycleOwner(), favoriteSubscribedUserData -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            if (favoriteSubscribedUserData != null && favoriteSubscribedUserData.size() > 0) {
                binding.noSubscriptionsLinearLayout.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(binding.noSubscriptionsImageView);
            }
            adapter.setFavoriteSubscribedUsers(favoriteSubscribedUserData);
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void stopRefreshProgressbar() {
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void applyTheme() {
        if (mActivity instanceof SubscribedThingListingActivity) {
            binding.swipeRefreshLayout.setOnRefreshListener(() -> ((SubscribedThingListingActivity) mActivity).loadSubscriptions(true));
            binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
            binding.swipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        } else {
            binding.swipeRefreshLayout.setEnabled(false);
        }
        binding.errorTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            binding.errorTextView.setTypeface(mActivity.typeface);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void changeSearchQuery(String searchQuery) {
        mSubscribedUserViewModel.setSearchQuery(searchQuery);
    }
}
