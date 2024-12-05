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
import ml.docilealligator.infinityforreddit.activities.MultiredditSelectionActivity;
import ml.docilealligator.infinityforreddit.activities.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.activities.ViewMultiRedditDetailActivity;
import ml.docilealligator.infinityforreddit.adapters.MultiRedditListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.MultiRedditOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentMultiRedditListingBinding;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class MultiRedditListingFragment extends Fragment implements FragmentCommunicator {
    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_IS_GETTING_MULTIREDDIT_INFO = "EIGMI";

    private FragmentMultiRedditListingBinding binding;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;

    public MultiRedditViewModel mMultiRedditViewModel;
    private BaseActivity mActivity;
    private RequestManager mGlide;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;

    public MultiRedditListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMultiRedditListingBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        applyTheme();

        if ((mActivity != null && ((BaseActivity) mActivity).isImmersiveInterface())) {
            binding.recyclerView.setPadding(0, 0, 0, ((BaseActivity) mActivity).getNavBarHeight());
        } else if (mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            Resources resources = getResources();
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME, "-");
        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        boolean isGettingMultiredditInfo = getArguments().getBoolean(EXTRA_IS_GETTING_MULTIREDDIT_INFO, false);

        if (accessToken == null) {
            binding.swipeRefreshLayout.setEnabled(false);
        }

        mGlide = Glide.with(this);

        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        binding.recyclerView.setLayoutManager(mLinearLayoutManager);
        MultiRedditListingRecyclerViewAdapter adapter = new MultiRedditListingRecyclerViewAdapter(mActivity,
                mExecutor, mOauthRetrofit, mRedditDataRoomDatabase, mCustomThemeWrapper, accessToken,
                new MultiRedditListingRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(MultiReddit multiReddit) {
                if (mActivity instanceof MultiredditSelectionActivity) {
                    ((MultiredditSelectionActivity) mActivity).getSelectedMultireddit(multiReddit);
                } else {
                    Intent intent = new Intent(mActivity, ViewMultiRedditDetailActivity.class);
                    intent.putExtra(ViewMultiRedditDetailActivity.EXTRA_MULTIREDDIT_DATA, multiReddit);
                    mActivity.startActivity(intent);
                }
            }

            @Override
            public void onLongClick(MultiReddit multiReddit) {
                if (!isGettingMultiredditInfo) {
                    showOptionsBottomSheetFragment(multiReddit);
                }
            }
        });
        binding.recyclerView.setAdapter(adapter);
        if (mActivity instanceof SubscribedThingListingActivity) {
            binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0) {
                        ((SubscribedThingListingActivity) mActivity).hideFabInMultiredditTab();
                    } else {
                        ((SubscribedThingListingActivity) mActivity).showFabInMultiredditTab();
                    }
                }
            });
        }
        new FastScrollerBuilder(binding.recyclerView).useMd2Style().build();

        mMultiRedditViewModel = new ViewModelProvider(this,
                new MultiRedditViewModel.Factory(mActivity.getApplication(), mRedditDataRoomDatabase, accountName))
                .get(MultiRedditViewModel.class);

        mMultiRedditViewModel.getAllMultiReddits().observe(getViewLifecycleOwner(), subscribedUserData -> {
            if (subscribedUserData == null || subscribedUserData.size() == 0) {
                binding.recyclerView.setVisibility(View.GONE);
                binding.fetchMultiRedditListingInfoLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(binding.fetchMultiRedditListingInfoImageView);
            } else {
                binding.fetchMultiRedditListingInfoLinearLayout.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(binding.fetchMultiRedditListingInfoImageView);
            }
            adapter.setMultiReddits(subscribedUserData);
        });

        mMultiRedditViewModel.getAllFavoriteMultiReddits().observe(getViewLifecycleOwner(), favoriteSubscribedUserData -> {
            if (favoriteSubscribedUserData != null && favoriteSubscribedUserData.size() > 0) {
                binding.fetchMultiRedditListingInfoLinearLayout.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(binding.fetchMultiRedditListingInfoImageView);
            }
            adapter.setFavoriteMultiReddits(favoriteSubscribedUserData);
        });

        return rootView;
    }

    private void showOptionsBottomSheetFragment(MultiReddit multiReddit) {
        MultiRedditOptionsBottomSheetFragment fragment = new MultiRedditOptionsBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(MultiRedditOptionsBottomSheetFragment.EXTRA_MULTI_REDDIT, multiReddit);
        fragment.setArguments(bundle);
        fragment.show(mActivity.getSupportFragmentManager(), fragment.getTag());
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void changeSearchQuery(String searchQuery) {
        mMultiRedditViewModel.setSearchQuery(searchQuery);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
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

        binding.fetchMultiRedditListingInfoTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
    }

    @Override
    public void stopRefreshProgressbar() {
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}