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
import ml.docilealligator.infinityforreddit.asynctasks.CheckIsSubscribedToSubreddit;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterErrorBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterLoadingBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemSubredditListingBinding;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditSubscription;
import retrofit2.Retrofit;

public class SubredditListingRecyclerViewAdapter extends PagedListAdapter<SubredditData, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<SubredditData> DIFF_CALLBACK = new DiffUtil.ItemCallback<SubredditData>() {
        @Override
        public boolean areItemsTheSame(@NonNull SubredditData oldItem, @NonNull SubredditData newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SubredditData oldItem, @NonNull SubredditData newItem) {
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
    private int colorPrimaryLightTheme;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int colorAccent;
    private int buttonTextColor;
    private int unsubscribed;

    private NetworkState networkState;
    private Callback callback;

    public SubredditListingRecyclerViewAdapter(BaseActivity activity, Executor executor, Retrofit oauthRetrofit, Retrofit retrofit,
                                               CustomThemeWrapper customThemeWrapper,
                                               String accessToken, String accountName,
                                               RedditDataRoomDatabase redditDataRoomDatabase,
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
        glide = Glide.with(this.activity);
        colorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        colorAccent = customThemeWrapper.getColorAccent();
        buttonTextColor = customThemeWrapper.getButtonTextColor();
        unsubscribed = customThemeWrapper.getUnsubscribed();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subreddit_listing, parent, false);
            return new DataViewHolder(constraintLayout);
        } else if (viewType == VIEW_TYPE_ERROR) {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false);
            return new ErrorViewHolder(relativeLayout);
        } else {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false);
            return new LoadingViewHolder(relativeLayout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            SubredditData subredditData = getItem(position);
            if (subredditData != null) {
                if (isMultiSelection) {
                    ((DataViewHolder) holder).binding.checkbox.setOnCheckedChangeListener((compoundButton, b) -> subredditData.setSelected(b));
                }
                ((DataViewHolder) holder).binding.constraintLayout.setOnClickListener(view -> {
                    if (isMultiSelection) {
                        ((DataViewHolder) holder).binding.checkbox.performClick();
                    } else {
                        callback.subredditSelected(subredditData.getName(), subredditData.getIconUrl());
                    }
                });

                if (!subredditData.getIconUrl().equals("")) {
                    glide.load(subredditData.getIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((DataViewHolder) holder).binding.subredditIconGifImageView);
                } else {
                    glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((DataViewHolder) holder).binding.subredditIconGifImageView);
                }

                ((DataViewHolder) holder).binding.subredditNameTextView.setText(subredditData.getName());
                ((DataViewHolder) holder).binding.subscriberCountTextView.setText(activity.getString(R.string.subscribers_number_detail, subredditData.getNSubscribers()));

                if (!isMultiSelection) {
                    CheckIsSubscribedToSubreddit.checkIsSubscribedToSubreddit(executor, new Handler(),
                            redditDataRoomDatabase, subredditData.getName(), accountName,
                            new CheckIsSubscribedToSubreddit.CheckIsSubscribedToSubredditListener() {
                                @Override
                                public void isSubscribed() {
                                    ((DataViewHolder) holder).binding.subscribeImageView.setVisibility(View.GONE);
                                }

                                @Override
                                public void isNotSubscribed() {
                                    ((DataViewHolder) holder).binding.subscribeImageView.setVisibility(View.VISIBLE);
                                    ((DataViewHolder) holder).binding.subscribeImageView.setOnClickListener(view -> {
                                        if (accessToken != null) {
                                            SubredditSubscription.subscribeToSubreddit(executor, new Handler(),
                                                    oauthRetrofit, retrofit, accessToken, subredditData.getName(), subredditData.getId(),
                                                    accountName, redditDataRoomDatabase,
                                                    new SubredditSubscription.SubredditSubscriptionListener() {
                                                        @Override
                                                        public void onSubredditSubscriptionSuccess() {
                                                            ((DataViewHolder) holder).binding.subscribeImageView.setVisibility(View.GONE);
                                                            Toast.makeText(activity, R.string.subscribed, Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onSubredditSubscriptionFail() {
                                                            Toast.makeText(activity, R.string.subscribe_failed, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            SubredditSubscription.anonymousSubscribeToSubreddit(executor, new Handler(), accessToken,
                                                    oauthRetrofit, redditDataRoomDatabase, subredditData.getName(),
                                                    new SubredditSubscription.SubredditSubscriptionListener() {
                                                        @Override
                                                        public void onSubredditSubscriptionSuccess() {
                                                            ((DataViewHolder) holder).binding.subscribeImageView.setVisibility(View.GONE);
                                                            Toast.makeText(activity, R.string.subscribed, Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onSubredditSubscriptionFail() {
                                                            Toast.makeText(activity, R.string.subscribe_failed, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                                }
                            });
                } else {
                    ((DataViewHolder) holder).binding.checkbox.setChecked(subredditData.isSelected());
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
            glide.clear(((DataViewHolder) holder).binding.subredditIconGifImageView);
            ((DataViewHolder) holder).binding.subscribeImageView.setVisibility(View.GONE);
        }
    }

    public interface Callback {
        void retryLoadingMore();

        void subredditSelected(String subredditName, String iconUrl);
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubredditListingBinding binding;

        DataViewHolder(View itemView) {
            super(itemView);
            binding = ItemSubredditListingBinding.bind(itemView);
            binding.subredditNameTextView.setTextColor(primaryTextColor);
            binding.subscriberCountTextView.setTextColor(secondaryTextColor);
            binding.subscribeImageView.setColorFilter(unsubscribed, android.graphics.PorterDuff.Mode.SRC_IN);
            if (isMultiSelection) {
                binding.checkbox.setVisibility(View.VISIBLE);
            }

            if (activity.typeface != null) {
                binding.subredditNameTextView.setTypeface(activity.typeface);
                binding.subscriberCountTextView.setTypeface(activity.typeface);
            }
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        ErrorViewHolder(View itemView) {
            super(itemView);
            ItemFooterErrorBinding binding = ItemFooterErrorBinding.bind(itemView);
            binding.retryButton.setOnClickListener(view -> callback.retryLoadingMore());
            binding.errorTextView.setText(R.string.load_comments_failed);
            binding.errorTextView.setTextColor(secondaryTextColor);
            binding.retryButton.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryLightTheme));
            binding.retryButton.setTextColor(buttonTextColor);

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
