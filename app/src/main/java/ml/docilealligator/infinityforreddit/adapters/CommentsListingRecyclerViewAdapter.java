package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.VoteThing;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CommentMoreBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.CustomMarkwonAdapter;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.SpoilerOnClickTextView;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockInterface;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterErrorBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterLoadingBinding;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CommentsListingRecyclerViewAdapter extends PagedListAdapter<Comment, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Comment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Comment comment, @NonNull Comment t1) {
            return comment.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment comment, @NonNull Comment t1) {
            return comment.getCommentMarkdown().equals(t1.getCommentMarkdown());
        }
    };
    private BaseActivity mActivity;
    private Retrofit mOauthRetrofit;
    private Retrofit mGqlRetrofit;
    private Locale mLocale;
    private Markwon mMarkwon;
    private RecyclerView.RecycledViewPool recycledViewPool;
    private String mAccessToken;
    private String mAccountName;
    private int mColorPrimaryLightTheme;
    private int mSecondaryTextColor;
    private int mCommentBackgroundColor;
    private int mCommentColor;
    private int mDividerColor;
    private int mUsernameColor;
    private int mAuthorFlairColor;
    private int mSubredditColor;
    private int mUpvotedColor;
    private int mDownvotedColor;
    private int mButtonTextColor;
    private int mColorAccent;
    private int mCommentIconAndInfoColor;
    private boolean mVoteButtonsOnTheRight;
    private boolean mShowElapsedTime;
    private String mTimeFormatPattern;
    private boolean mShowCommentDivider;
    private boolean mShowAbsoluteNumberOfVotes;
    private NetworkState networkState;
    private RetryLoadingMoreCallback mRetryLoadingMoreCallback;

    public CommentsListingRecyclerViewAdapter(BaseActivity activity, Retrofit oauthRetrofit, Retrofit gqlRetrofit,
                                              CustomThemeWrapper customThemeWrapper, Locale locale,
                                              SharedPreferences sharedPreferences, String accessToken,
                                              String accountName, RetryLoadingMoreCallback retryLoadingMoreCallback) {
        super(DIFF_CALLBACK);
        mActivity = activity;
        mOauthRetrofit = oauthRetrofit;
        mGqlRetrofit = gqlRetrofit;
        mCommentColor = customThemeWrapper.getCommentColor();
        int commentSpoilerBackgroundColor = mCommentColor | 0xFF000000;
        mLocale = locale;
        mAccessToken = accessToken;
        mAccountName = accountName;
        mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
        mShowCommentDivider = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_COMMENT_DIVIDER, false);
        mShowAbsoluteNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES, true);
        mVoteButtonsOnTheRight = sharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
        mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
        mRetryLoadingMoreCallback = retryLoadingMoreCallback;
        mColorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        mCommentBackgroundColor = customThemeWrapper.getCommentBackgroundColor();
        mCommentColor = customThemeWrapper.getCommentColor();
        mDividerColor = customThemeWrapper.getDividerColor();
        mSubredditColor = customThemeWrapper.getSubreddit();
        mUsernameColor = customThemeWrapper.getUsername();
        mAuthorFlairColor = customThemeWrapper.getAuthorFlairTextColor();
        mUpvotedColor = customThemeWrapper.getUpvoted();
        mDownvotedColor = customThemeWrapper.getDownvoted();
        mButtonTextColor = customThemeWrapper.getButtonTextColor();
        mColorAccent = customThemeWrapper.getColorAccent();
        mCommentIconAndInfoColor = customThemeWrapper.getCommentIconAndInfoColor();
        int linkColor = customThemeWrapper.getLinkColor();
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (mActivity.contentTypeface != null) {
                    textView.setTypeface(mActivity.contentTypeface);
                }
                textView.setTextColor(mCommentColor);
                textView.setHighlightColor(Color.TRANSPARENT);
            }

            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    mActivity.startActivity(intent);
                });
            }

            @Override
            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                builder.linkColor(linkColor);
            }
        };
        BetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener = (textView, url) -> {
            if (!activity.isDestroyed() && !activity.isFinishing()) {
                UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url);
                urlMenuBottomSheetFragment.show(activity.getSupportFragmentManager(), null);
            }
            return true;
        };
        mMarkwon = MarkdownUtils.createFullRedditMarkwon(mActivity,
                miscPlugin, mCommentColor, commentSpoilerBackgroundColor, onLinkLongClickListener);
        recycledViewPool = new RecyclerView.RecycledViewPool();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            Comment comment = getItem(holder.getBindingAdapterPosition());
            if (comment != null) {
                String name = "r/" + comment.getSubredditName();
                ((CommentViewHolder) holder).binding.authorTextView.setText(name);
                ((CommentViewHolder) holder).binding.authorTextView.setTextColor(mSubredditColor);

                if (comment.getAuthorFlairHTML() != null && !comment.getAuthorFlairHTML().equals("")) {
                    ((CommentViewHolder) holder).binding.authorFlairTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((CommentViewHolder) holder).binding.authorFlairTextView, comment.getAuthorFlairHTML(), true);
                } else if (comment.getAuthorFlair() != null && !comment.getAuthorFlair().equals("")) {
                    ((CommentViewHolder) holder).binding.authorFlairTextView.setVisibility(View.VISIBLE);
                    ((CommentViewHolder) holder).binding.authorFlairTextView.setText(comment.getAuthorFlair());
                }

                if (mShowElapsedTime) {
                    ((CommentViewHolder) holder).binding.commentTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
                } else {
                    ((CommentViewHolder) holder).binding.commentTimeTextView.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
                }

                if (comment.getAwards() != null && !comment.getAwards().equals("")) {
                    ((CommentViewHolder) holder).binding.awardsTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((CommentViewHolder) holder).binding.awardsTextView, comment.getAwards(), true);
                }

                ((CommentViewHolder) holder).markwonAdapter.setMarkdown(mMarkwon, comment.getCommentMarkdown());
                // noinspection NotifyDataSetChanged
                ((CommentViewHolder) holder).markwonAdapter.notifyDataSetChanged();

                String commentText = "";
                if (comment.isScoreHidden()) {
                    commentText = mActivity.getString(R.string.hidden);
                } else {
                    commentText = Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                            comment.getScore() + comment.getVoteType());
                }
                ((CommentViewHolder) holder).binding.scoreTextView.setText(commentText);

                switch (comment.getVoteType()) {
                    case Comment.VOTE_TYPE_UPVOTE:
                        ((CommentViewHolder) holder).binding.upvoteButton
                                .setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).binding.scoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case Comment.VOTE_TYPE_DOWNVOTE:
                        ((CommentViewHolder) holder).binding.downvoteButton
                                .setColorFilter(mDownvotedColor, PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).binding.scoreTextView.setTextColor(mDownvotedColor);
                        break;
                }

                if (comment.isSaved()) {
                    ((CommentViewHolder) holder).binding.saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((CommentViewHolder) holder).binding.saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
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
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).binding.authorFlairTextView.setText("");
            ((CommentViewHolder) holder).binding.authorFlairTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).binding.awardsTextView.setText("");
            ((CommentViewHolder) holder).binding.awardsTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
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

    public void onItemSwipe(RecyclerView.ViewHolder viewHolder, int direction, int swipeLeftAction, int swipeRightAction) {
        if (viewHolder instanceof CommentViewHolder) {
            if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.START) {
                if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((CommentViewHolder) viewHolder).binding.upvoteButton.performClick();
                } else if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((CommentViewHolder) viewHolder).binding.downvoteButton.performClick();
                }
            } else {
                if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((CommentViewHolder) viewHolder).binding.upvoteButton.performClick();
                } else if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((CommentViewHolder) viewHolder).binding.downvoteButton.performClick();
                }
            }
        }
    }

    public void giveAward(String awardsHTML, int position) {
        if (position >= 0 && position < getItemCount()) {
            Comment comment = getItem(position);
            if (comment != null) {
                comment.addAwards(awardsHTML);
                notifyItemChanged(position);
            }
        }
    }

    public void editComment(String commentContentMarkdown, int position) {
        Comment comment = getItem(position);
        if (comment != null) {
            comment.setCommentMarkdown(commentContentMarkdown);
            notifyItemChanged(position);
        }
    }

    public interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;

        CustomMarkwonAdapter markwonAdapter;

        CommentViewHolder(View itemView) {
            super(itemView);
            binding = ItemCommentBinding.bind(itemView);

            binding.replyButton.setVisibility(View.GONE);

            ((ConstraintLayout.LayoutParams) binding.authorTextView.getLayoutParams()).setMarginStart(0);
            ((ConstraintLayout.LayoutParams) binding.authorFlairTextView.getLayoutParams()).setMarginStart(0);

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(binding.bottomConstraintLayout);
                constraintSet.clear(binding.upvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(binding.upvoteButton.getId(), ConstraintSet.END);
                constraintSet.clear(binding.scoreTextView.getId(), ConstraintSet.START);
                constraintSet.clear(binding.scoreTextView.getId(), ConstraintSet.END);
                constraintSet.clear(binding.downvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(binding.downvoteButton.getId(), ConstraintSet.END);
                constraintSet.clear(binding.expandButton.getId(), ConstraintSet.START);
                constraintSet.clear(binding.expandButton.getId(), ConstraintSet.END);
                constraintSet.clear(binding.saveButton.getId(), ConstraintSet.START);
                constraintSet.clear(binding.saveButton.getId(), ConstraintSet.END);
                constraintSet.clear(binding.replyButton.getId(), ConstraintSet.START);
                constraintSet.clear(binding.replyButton.getId(), ConstraintSet.END);
                constraintSet.clear(binding.moreButton.getId(), ConstraintSet.START);
                constraintSet.clear(binding.moreButton.getId(), ConstraintSet.END);
                constraintSet.connect(binding.upvoteButton.getId(), ConstraintSet.END, binding.scoreTextView.getId(), ConstraintSet.START);
                constraintSet.connect(binding.upvoteButton.getId(), ConstraintSet.START, binding.placeholder.getId(), ConstraintSet.END);
                constraintSet.connect(binding.scoreTextView.getId(), ConstraintSet.END, binding.downvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(binding.scoreTextView.getId(), ConstraintSet.START, binding.upvoteButton.getId(), ConstraintSet.END);
                constraintSet.connect(binding.downvoteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(binding.downvoteButton.getId(), ConstraintSet.START, binding.scoreTextView.getId(), ConstraintSet.END);
                constraintSet.connect(binding.placeholder.getId(), ConstraintSet.END, binding.upvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(binding.placeholder.getId(), ConstraintSet.START, binding.moreButton.getId(), ConstraintSet.END);
                constraintSet.connect(binding.moreButton.getId(), ConstraintSet.START, binding.expandButton.getId(), ConstraintSet.END);
                constraintSet.connect(binding.moreButton.getId(), ConstraintSet.END, binding.placeholder.getId(), ConstraintSet.START);
                constraintSet.connect(binding.expandButton.getId(), ConstraintSet.START, binding.saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(binding.expandButton.getId(), ConstraintSet.END, binding.moreButton.getId(), ConstraintSet.START);
                constraintSet.connect(binding.saveButton.getId(), ConstraintSet.START, binding.replyButton.getId(), ConstraintSet.END);
                constraintSet.connect(binding.saveButton.getId(), ConstraintSet.END, binding.expandButton.getId(), ConstraintSet.START);
                constraintSet.connect(binding.replyButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(binding.replyButton.getId(), ConstraintSet.END, binding.saveButton.getId(), ConstraintSet.START);
                constraintSet.applyTo(binding.bottomConstraintLayout);
            }

            binding.linearLayout.getLayoutTransition().setAnimateParentHierarchy(false);

            binding.commentIndentationView.setVisibility(View.GONE);

            if (mShowCommentDivider) {
                binding.divider.setVisibility(View.VISIBLE);
            }

            if (mActivity.typeface != null) {
                binding.authorTextView.setTypeface(mActivity.typeface);
                binding.authorFlairTextView.setTypeface(mActivity.typeface);
                binding.commentTimeTextView.setTypeface(mActivity.typeface);
                binding.awardsTextView.setTypeface(mActivity.typeface);
                binding.scoreTextView.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mCommentBackgroundColor);
            binding.authorTextView.setTextColor(mUsernameColor);
            binding.authorFlairTextView.setTextColor(mAuthorFlairColor);
            binding.commentTimeTextView.setTextColor(mSecondaryTextColor);
            binding.awardsTextView.setTextColor(mSecondaryTextColor);
            binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
            binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            binding.moreButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            binding.saveButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            binding.replyButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            binding.divider.setBackgroundColor(mDividerColor);

            binding.authorTextView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, comment.getSubredditName());
                    mActivity.startActivity(intent);
                }
            });

            binding.moreButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    Bundle bundle = new Bundle();
                    if (comment.getAuthor().equals(mAccountName)) {
                        bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_EDIT_AND_DELETE_AVAILABLE, true);
                    }
                    bundle.putString(CommentMoreBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    bundle.putParcelable(CommentMoreBottomSheetFragment.EXTRA_COMMENT, comment);
                    bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition());
                    CommentMoreBottomSheetFragment commentMoreBottomSheetFragment = new CommentMoreBottomSheetFragment();
                    commentMoreBottomSheetFragment.setArguments(bundle);
                    commentMoreBottomSheetFragment.show(mActivity.getSupportFragmentManager(), commentMoreBottomSheetFragment.getTag());
                }
            });

            itemView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    Intent intent = new Intent(mActivity, ViewPostDetailActivity.class);
                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, comment.getLinkId());
                    intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, comment.getId());
                    mActivity.startActivity(intent);
                }
            });

            binding.commentMarkdownView.setRecycledViewPool(recycledViewPool);
            LinearLayoutManagerBugFixed linearLayoutManager = new SwipeLockLinearLayoutManager(mActivity, new SwipeLockInterface() {
                @Override
                public void lockSwipe() {
                    mActivity.lockSwipeRightToGoBack();
                }

                @Override
                public void unlockSwipe() {
                    mActivity.unlockSwipeRightToGoBack();
                }
            });
            binding.commentMarkdownView.setLayoutManager(linearLayoutManager);
            markwonAdapter = MarkdownUtils.createCustomTablesAdapter();
            markwonAdapter.setOnClickListener(view -> {
                if (view instanceof SpoilerOnClickTextView) {
                    if (((SpoilerOnClickTextView) view).isSpoilerOnClick()) {
                        ((SpoilerOnClickTextView) view).setSpoilerOnClick(false);
                        return;
                    }
                }
                itemView.callOnClick();
            });
            binding.commentMarkdownView.setAdapter(markwonAdapter);

            binding.upvoteButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != Comment.VOTE_TYPE_UPVOTE) {
                        //Not upvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                        newVoteType = APIUtils.DIR_UPVOTE;
                        binding.upvoteButton
                                .setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                        binding.scoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                        binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
                    }

                    if (!comment.isScoreHidden()) {
                        binding.scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                    }

                    VoteThing.voteThing(mActivity, mGqlRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                                if (currentPosition == position) {
                                    binding.upvoteButton.setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                                    binding.scoreTextView.setTextColor(mUpvotedColor);
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                    binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                if (!comment.isScoreHidden()) {
                                    binding.scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                            comment.getScore() + comment.getVoteType()));
                                }
                            }
                        }

                        @Override
                        public void onVoteThingFail(int position) {
                        }
                    }, comment.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            binding.downvoteButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != Comment.VOTE_TYPE_DOWNVOTE) {
                        //Not downvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_DOWNVOTE);
                        newVoteType = APIUtils.DIR_DOWNVOTE;
                        binding.downvoteButton.setColorFilter(mDownvotedColor, PorterDuff.Mode.SRC_IN);
                        binding.scoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                        binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
                    }

                    if (!comment.isScoreHidden()) {
                        binding.scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                    }

                    VoteThing.voteThing(mActivity, mGqlRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                                comment.setVoteType(Comment.VOTE_TYPE_DOWNVOTE);
                                if (currentPosition == position) {
                                    binding.downvoteButton.setColorFilter(mDownvotedColor, PorterDuff.Mode.SRC_IN);
                                    binding.scoreTextView.setTextColor(mDownvotedColor);
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                    binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                if (!comment.isScoreHidden()) {
                                    binding.scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                            comment.getScore() + comment.getVoteType()));
                                }
                            }
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                        }
                    }, comment.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            binding.saveButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(position);
                if (comment != null) {
                    if (comment.isSaved()) {
                        comment.setSaved(false);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(false);
                                if (getBindingAdapterPosition() == position) {
                                    binding.saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_unsaved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(true);
                                if (getBindingAdapterPosition() == position) {
                                    binding.saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_unsaved_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        comment.setSaved(true);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(true);
                                if (getBindingAdapterPosition() == position) {
                                    binding.saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_saved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(false);
                                if (getBindingAdapterPosition() == position) {
                                    binding.saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_saved_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        ErrorViewHolder(View itemView) {
            super(itemView);
            ItemFooterErrorBinding binding = ItemFooterErrorBinding.bind(itemView);
            if (mActivity.typeface != null) {
                binding.errorTextView.setTypeface(mActivity.typeface);
                binding.retryButton.setTypeface(mActivity.typeface);
            }
            binding.errorTextView.setText(R.string.load_comments_failed);
            binding.retryButton.setOnClickListener(view -> mRetryLoadingMoreCallback.retryLoadingMore());
            binding.errorTextView.setTextColor(mSecondaryTextColor);
            binding.retryButton.setBackgroundTintList(ColorStateList.valueOf(mColorPrimaryLightTheme));
            binding.retryButton.setTextColor(mButtonTextColor);
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemFooterLoadingBinding binding = ItemFooterLoadingBinding.bind(itemView);
            binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
        }
    }
}
