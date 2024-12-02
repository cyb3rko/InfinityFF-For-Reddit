package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.concurrent.Executor;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.asynctasks.CheckIsFollowingUser;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterErrorBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterLoadingBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemUserListingBinding;
import ml.docilealligator.infinityforreddit.user.UserData;
import ml.docilealligator.infinityforreddit.user.UserFollowing;
import retrofit2.Retrofit;

public class UserListingRecyclerViewAdapter extends PagedListAdapter<UserData, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<UserData> DIFF_CALLBACK = new DiffUtil.ItemCallback<UserData>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserData oldItem, @NonNull UserData newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserData oldItem, @NonNull UserData newItem) {
            return true;
        }
    };
    private RequestManager glide;
    private BaseActivity activity;
    private Executor executor;
    private Retrofit oauthRetrofit;
    private Retrofit retrofit;
    private String accessToken;
    private String accountName;
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private boolean isMultiSelection;

    private int primaryTextColor;
    private int buttonTextColor;
    private int colorPrimaryLightTheme;
    private int colorAccent;
    private int unsubscribedColor;

    private NetworkState networkState;
    private final Callback callback;

    public UserListingRecyclerViewAdapter(BaseActivity activity, Executor executor, Retrofit oauthRetrofit, Retrofit retrofit,
                                          CustomThemeWrapper customThemeWrapper, String accessToken,
                                          String accountName, RedditDataRoomDatabase redditDataRoomDatabase,
                                          boolean isMultiSelection, Callback callback) {
        super(DIFF_CALLBACK);
        this.activity = activity;
        this.executor = executor;
        this.oauthRetrofit = oauthRetrofit;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.isMultiSelection = isMultiSelection;
        this.callback = callback;
        glide = Glide.with(activity);
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        buttonTextColor = customThemeWrapper.getButtonTextColor();
        colorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        colorAccent = customThemeWrapper.getColorAccent();
        unsubscribedColor = customThemeWrapper.getUnsubscribed();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_listing, parent, false);
            return new DataViewHolder(constraintLayout);
        } else if (viewType == VIEW_TYPE_ERROR) {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false);
            return new UserListingRecyclerViewAdapter.ErrorViewHolder(relativeLayout);
        } else {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false);
            return new UserListingRecyclerViewAdapter.LoadingViewHolder(relativeLayout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            UserData userData = getItem(position);
            if (userData != null) {
                ((DataViewHolder) holder).binding.constraintLayout.setOnClickListener(view -> {
                    if (isMultiSelection) {
                        ((DataViewHolder) holder).binding.checkbox.performClick();
                    } else {
                        callback.userSelected(userData.getName(), userData.getIconUrl());
                    }
                });

                if (!userData.getIconUrl().equals("")) {
                    glide.load(userData.getIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((DataViewHolder) holder).binding.userIconGifImageView);
                } else {
                    glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((DataViewHolder) holder).binding.userIconGifImageView);
                }

                ((DataViewHolder) holder).binding.userNameTextView.setText(userData.getName());

                if (!isMultiSelection) {
                    CheckIsFollowingUser.checkIsFollowingUser(executor, new Handler(), redditDataRoomDatabase,
                            userData.getName(), accountName, new CheckIsFollowingUser.CheckIsFollowingUserListener() {
                                @Override
                                public void isSubscribed() {
                                    ((DataViewHolder) holder).binding.subscribeImageView.setVisibility(View.GONE);
                                }

                                @Override
                                public void isNotSubscribed() {
                                    ((DataViewHolder) holder).binding.subscribeImageView.setVisibility(View.VISIBLE);
                                    ((DataViewHolder) holder).binding.subscribeImageView.setOnClickListener(view -> {
                                        UserFollowing.followUser(oauthRetrofit, retrofit,
                                                accessToken, userData.getName(), accountName, redditDataRoomDatabase,
                                                new UserFollowing.UserFollowingListener() {
                                                    @Override
                                                    public void onUserFollowingSuccess() {
                                                        ((DataViewHolder) holder).binding.subscribeImageView.setVisibility(View.GONE);
                                                        Toast.makeText(activity, R.string.followed, Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onUserFollowingFail() {
                                                        Toast.makeText(activity, R.string.follow_failed, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    });
                                }
                            });
                } else {
                    ((DataViewHolder) holder).binding.checkbox.setOnCheckedChangeListener((compoundButton, b) -> userData.setSelected(b));
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Reached at the end
        if (hasExtraRow() && position == getItemCount() - 1) {
            if (networkState.getStatus() == NetworkState.Status.LOADING) {
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_ERROR;
            }
        } else {
            return VIEW_TYPE_DATA;
        }
    }

    @Override
    public int getItemCount() {
        if (hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState.getStatus() != NetworkState.Status.SUCCESS;
    }

    public void setNetworkState(NetworkState newNetworkState) {
        NetworkState previousState = this.networkState;
        boolean previousExtraRow = hasExtraRow();
        this.networkState = newNetworkState;
        boolean newExtraRow = hasExtraRow();
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(super.getItemCount());
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (newExtraRow && !previousState.equals(newNetworkState)) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof DataViewHolder) {
            glide.clear(((DataViewHolder) holder).binding.userIconGifImageView);
            ((DataViewHolder) holder).binding.subscribeImageView.setVisibility(View.GONE);
        }
    }

    public interface Callback {
        void retryLoadingMore();

        void userSelected(String username, String iconUrl);
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserListingBinding binding;

        DataViewHolder(View itemView) {
            super(itemView);
            binding = ItemUserListingBinding.bind(itemView);
            binding.userNameTextView.setTextColor(primaryTextColor);
            binding.subscribeImageView.setColorFilter(unsubscribedColor, android.graphics.PorterDuff.Mode.SRC_IN);

            if (activity.typeface != null) {
                binding.userNameTextView.setTypeface(activity.typeface);
            }

            if (isMultiSelection) {
                binding.checkbox.setVisibility(View.VISIBLE);
            }
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        ErrorViewHolder(View itemView) {
            super(itemView);
            ItemFooterErrorBinding binding = ItemFooterErrorBinding.bind(itemView);
            binding.retryButton.setOnClickListener(view -> callback.retryLoadingMore());
            binding.errorTextView.setText(R.string.load_comments_failed);
            binding.errorTextView.setTextColor(primaryTextColor);
            binding.retryButton.setTextColor(buttonTextColor);
            binding.retryButton.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryLightTheme));

            if (activity.typeface != null) {
                binding.retryButton.setTypeface(activity.typeface);
                binding.errorTextView.setTypeface(activity.typeface);
            }
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemFooterLoadingBinding binding = ItemFooterLoadingBinding.bind(itemView);
            binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(colorAccent));
        }
    }
}
