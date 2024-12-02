package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.MessageRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentInboxBinding;
import ml.docilealligator.infinityforreddit.events.RepliedToPrivateMessageEvent;
import ml.docilealligator.infinityforreddit.message.FetchMessage;
import ml.docilealligator.infinityforreddit.message.Message;
import ml.docilealligator.infinityforreddit.message.MessageViewModel;
import retrofit2.Retrofit;

public class InboxFragment extends Fragment implements FragmentCommunicator {
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_MESSAGE_WHERE = "EMT";

    private FragmentInboxBinding binding;

    MessageViewModel mMessageViewModel;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String mAccessToken;
    private String mWhere;
    private MessageRecyclerViewAdapter mAdapter;
    private RequestManager mGlide;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private BaseActivity mActivity;

    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        EventBus.getDefault().register(this);

        applyTheme();

        Bundle arguments = getArguments();
        if (arguments == null) {
            return rootView;
        }
        mAccessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        mGlide = Glide.with(this);

        if (mActivity.isImmersiveInterface()) {
            binding.recyclerView.setPadding(0, 0, 0, mActivity.getNavBarHeight());
        }

        mWhere = arguments.getString(EXTRA_MESSAGE_WHERE, FetchMessage.WHERE_INBOX);
        mAdapter = new MessageRecyclerViewAdapter(mActivity, mOauthRetrofit, mCustomThemeWrapper,
                mAccessToken, mWhere, () -> mMessageViewModel.retryLoadingMore());
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        binding.recyclerView.setLayoutManager(mLinearLayoutManager);
        binding.recyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mActivity, mLinearLayoutManager.getOrientation());
        binding.recyclerView.addItemDecoration(dividerItemDecoration);

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

        MessageViewModel.Factory factory = new MessageViewModel.Factory(mOauthRetrofit,
                getResources().getConfiguration().locale, mAccessToken, mWhere);
        mMessageViewModel = new ViewModelProvider(this, factory).get(MessageViewModel.class);
        mMessageViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> mAdapter.submitList(messages));

        mMessageViewModel.hasMessage().observe(getViewLifecycleOwner(), hasMessage -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            if (hasMessage) {
                binding.fetchMessagesInfoLinearLayout.setVisibility(View.GONE);
            } else {
                binding.fetchMessagesInfoLinearLayout.setOnClickListener(null);
                showErrorView(R.string.no_messages);
            }
        });

        mMessageViewModel.getInitialLoadingState().observe(getViewLifecycleOwner(), networkState -> {
            if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                binding.swipeRefreshLayout.setRefreshing(false);
            } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.fetchMessagesInfoLinearLayout.setOnClickListener(view -> {
                    binding.fetchMessagesInfoLinearLayout.setVisibility(View.GONE);
                    mMessageViewModel.refresh();
                    mAdapter.setNetworkState(null);
                });
                showErrorView(R.string.load_messages_failed);
            } else {
                binding.swipeRefreshLayout.setRefreshing(true);
            }
        });

        mMessageViewModel.getPaginationNetworkState().observe(getViewLifecycleOwner(), networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        binding.swipeRefreshLayout.setOnRefreshListener(this::onRefresh);

        return rootView;
    }

    private void showErrorView(int stringResId) {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.fetchMessagesInfoLinearLayout.setVisibility(View.VISIBLE);
        binding.fetchMessagesInfoTextView.setText(stringResId);
        mGlide.load(R.drawable.error_image).into(binding.fetchMessagesInfoImageView);
    }

    @Override
    public void applyTheme() {
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        binding.fetchMessagesInfoTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            binding.fetchMessagesInfoTextView.setTypeface(mActivity.typeface);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void markAllMessagesRead() {
        if (mAdapter != null) {
            mAdapter.setMarkAllMessagesAsRead(true);

            int previousPosition = -1;
            if (mLinearLayoutManager != null) {
                previousPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
            }

            RecyclerView.LayoutManager layoutManager = binding.recyclerView.getLayoutManager();
            binding.recyclerView.setAdapter(null);
            binding.recyclerView.setLayoutManager(null);
            binding.recyclerView.setAdapter(mAdapter);
            binding.recyclerView.setLayoutManager(layoutManager);

            if (previousPosition > 0) {
                binding.recyclerView.scrollToPosition(previousPosition);
            }
        }
    }

    private void onRefresh() {
        mMessageViewModel.refresh();
        mAdapter.setNetworkState(null);
    }

    public Message getMessageByIndex(int index) {
        if (mMessageViewModel == null || index < 0) {
            return null;
        }
        PagedList<Message> messages = mMessageViewModel.getMessages().getValue();
        if (messages == null) {
            return null;
        }
        if (index >= messages.size()) {
            return null;
        }

        return messages.get(index);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    @Subscribe
    public void onRepliedToPrivateMessageEvent(RepliedToPrivateMessageEvent repliedToPrivateMessageEvent) {
        if (mAdapter != null && mWhere.equals(FetchMessage.WHERE_MESSAGES)) {
            mAdapter.updateMessageReply(repliedToPrivateMessageEvent.newReply, repliedToPrivateMessageEvent.messagePosition);
        }
    }
}