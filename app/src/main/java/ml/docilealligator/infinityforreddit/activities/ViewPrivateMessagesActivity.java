package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.evernote.android.state.State;
import com.google.android.material.snackbar.Snackbar;
import com.livefront.bridge.Bridge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.PrivateMessagesDetailRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.asynctasks.LoadUserData;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewPrivateMessagesBinding;
import ml.docilealligator.infinityforreddit.events.PassPrivateMessageEvent;
import ml.docilealligator.infinityforreddit.events.PassPrivateMessageIndexEvent;
import ml.docilealligator.infinityforreddit.events.RepliedToPrivateMessageEvent;
import ml.docilealligator.infinityforreddit.message.Message;
import ml.docilealligator.infinityforreddit.message.ReadMessage;
import ml.docilealligator.infinityforreddit.message.ReplyMessage;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class ViewPrivateMessagesActivity extends BaseActivity implements ActivityToolbarInterface {
    public static final String EXTRA_PRIVATE_MESSAGE_INDEX = "EPM";
    public static final String EXTRA_MESSAGE_POSITION = "EMP";
    private static final String USER_AVATAR_STATE = "UAS";

    private ActivityViewPrivateMessagesBinding binding;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private PrivateMessagesDetailRecyclerViewAdapter mAdapter;
    @State
    Message privateMessage;
    @State
    Message replyTo;
    private String mAccessToken;
    private String mAccountName;
    private String mUserAvatar;
    private ArrayList<ProvideUserAvatarCallback> mProvideUserAvatarCallbacks;
    private boolean isLoadingUserAvatar = false;
    private boolean isSendingMessage = false;
    private int mSecondaryTextColor;
    private int mSendMessageIconColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityViewPrivateMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bridge.restoreInstanceState(this, savedInstanceState);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appBarLayout);
        }

        setSupportActionBar(binding.toolbar);
        setToolbarGoToTop(binding.toolbar);

        mProvideUserAvatarCallbacks = new ArrayList<>();

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        if (savedInstanceState != null) {
            mUserAvatar = savedInstanceState.getString(USER_AVATAR_STATE);
            if (privateMessage == null) {
                EventBus.getDefault().post(new PassPrivateMessageIndexEvent(getIntent().getIntExtra(EXTRA_PRIVATE_MESSAGE_INDEX, -1)));
            } else {
                bindView();
            }
        } else {
            EventBus.getDefault().post(new PassPrivateMessageIndexEvent(getIntent().getIntExtra(EXTRA_PRIVATE_MESSAGE_INDEX, -1)));
        }
    }

    private void bindView() {
        if (privateMessage != null) {
            if (privateMessage.getAuthor().equals(mAccountName)) {
                setTitle(privateMessage.getDestination());
                binding.toolbar.setOnClickListener(view -> {
                    if (privateMessage.isDestinationDeleted()) {
                        return;
                    }
                    Intent intent = new Intent(this, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, privateMessage.getDestination());
                    startActivity(intent);
                });
            } else {
                setTitle(privateMessage.getAuthor());
                binding.toolbar.setOnClickListener(view -> {
                    if (privateMessage.isAuthorDeleted()) {
                        return;
                    }
                    Intent intent = new Intent(this, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, privateMessage.getAuthor());
                    startActivity(intent);
                });
            }
        }
        mAdapter = new PrivateMessagesDetailRecyclerViewAdapter(this, mSharedPreferences,
                getResources().getConfiguration().locale, privateMessage, mAccountName, mCustomThemeWrapper);
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(this);
        mLinearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(mLinearLayoutManager);
        binding.recyclerView.setAdapter(mAdapter);
        goToBottom();
        binding.sendImageView.setOnClickListener(view -> {
            if (!isSendingMessage) {
                if (!binding.editText.getText().toString().equals("")) {
                    //Send Message
                    if (privateMessage != null) {
                        ArrayList<Message> replies = privateMessage.getReplies();
                        if (replyTo == null) {
                            replyTo = privateMessage;
                        }
                        isSendingMessage = true;
                        binding.sendImageView.setColorFilter(mSecondaryTextColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ReplyMessage.replyMessage(binding.editText.getText().toString(), replyTo.getFullname(),
                                getResources().getConfiguration().locale, mOauthRetrofit, mAccessToken,
                                new ReplyMessage.ReplyMessageListener() {
                                    @Override
                                    public void replyMessageSuccess(Message message) {
                                        if (mAdapter != null) {
                                            mAdapter.addReply(message);
                                        }
                                        goToBottom();
                                        binding.editText.setText("");
                                        binding.sendImageView.setColorFilter(mSendMessageIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                        isSendingMessage = false;
                                        EventBus.getDefault().post(new RepliedToPrivateMessageEvent(message, getIntent().getIntExtra(EXTRA_MESSAGE_POSITION, -1)));
                                    }

                                    @Override
                                    public void replyMessageFailed(String errorMessage) {
                                        if (errorMessage != null && !errorMessage.equals("")) {
                                            Snackbar.make(binding.coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
                                        } else {
                                            Snackbar.make(binding.coordinatorLayout, R.string.reply_message_failed, Snackbar.LENGTH_LONG).show();
                                        }
                                        binding.sendImageView.setColorFilter(mSendMessageIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                        isSendingMessage = false;
                                    }
                                });
                        StringBuilder fullnames = new StringBuilder();
                        if (privateMessage.isNew()) {
                            fullnames.append(privateMessage.getFullname()).append(",");
                        }
                        if (replies != null && !replies.isEmpty()) {
                            for (Message m : replies) {
                                if (m.isNew()) {
                                    fullnames.append(m).append(",");
                                }
                            }
                        }
                        if (fullnames.length() > 0) {
                            fullnames.deleteCharAt(fullnames.length() - 1);
                            ReadMessage.readMessage(mOauthRetrofit, mAccessToken, fullnames.toString(),
                                    new ReadMessage.ReadMessageListener() {
                                        @Override
                                        public void readSuccess() {}

                                        @Override
                                        public void readFailed() {}
                                    });
                        }
                    }
                }
            }
        });
    }

    public void fetchUserAvatar(String username, ProvideUserAvatarCallback provideUserAvatarCallback) {
        if (mUserAvatar == null) {
            mProvideUserAvatarCallbacks.add(provideUserAvatarCallback);
            if (!isLoadingUserAvatar) {
                LoadUserData.loadUserData(mExecutor, new Handler(), mRedditDataRoomDatabase,
                        username, mRetrofit, iconImageUrl -> {
                    isLoadingUserAvatar = false;
                    mUserAvatar = iconImageUrl == null ? "" : iconImageUrl;
                    for (ProvideUserAvatarCallback provideUserAvatarCallbackInArrayList : mProvideUserAvatarCallbacks) {
                        provideUserAvatarCallbackInArrayList.fetchAvatarSuccess(iconImageUrl);
                    }
                    mProvideUserAvatarCallbacks.clear();
                });
            }
        } else {
            provideUserAvatarCallback.fetchAvatarSuccess(mUserAvatar);
        }
    }

    public void delayTransition() {
        TransitionManager.beginDelayedTransition(binding.recyclerView, new AutoTransition());
    }

    private void goToBottom() {
        if (mLinearLayoutManager != null && mAdapter != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(mAdapter.getItemCount() - 1, 0);
        }
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
        outState.putString(USER_AVATAR_STATE, mUserAvatar);
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Bridge.clear(this);
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
                binding.appBarLayout, null, binding.toolbar);
        binding.editTextDivider.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
        binding.editText.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        mSecondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.editText.setHintTextColor(mSecondaryTextColor);
        binding.editTextWrapperLinearLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        mSendMessageIconColor = mCustomThemeWrapper.getSendMessageIconColor();
        binding.sendImageView.setColorFilter(mSendMessageIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onLongPress() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    @Subscribe
    public void onPassPrivateMessageEvent(PassPrivateMessageEvent passPrivateMessageEvent) {
        privateMessage = passPrivateMessageEvent.message;
        if (privateMessage != null) {
            if (privateMessage.getAuthor().equals(mAccountName)) {
                if (privateMessage.getReplies() != null) {
                    for (int i = privateMessage.getReplies().size() - 1; i >= 0; i--) {
                        if (!privateMessage.getReplies().get(i).getAuthor().equals(mAccountName)) {
                            replyTo = privateMessage.getReplies().get(i);
                            break;
                        }
                    }
                }
                if (replyTo == null) {
                    replyTo = privateMessage;
                }
            } else {
                replyTo = privateMessage;
            }

            bindView();
        }
    }

    public interface ProvideUserAvatarCallback {
        void fetchAvatarSuccess(String userAvatarUrl);
    }
}