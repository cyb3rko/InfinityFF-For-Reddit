package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.VoteThing;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CommentMoreBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.comment.FetchComment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.CustomMarkwonAdapter;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.SpoilerOnClickTextView;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockInterface;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentFooterErrorBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentFooterLoadingBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentFullyCollapsedBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemLoadCommentsBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemLoadCommentsFailedPlaceholderBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemLoadMoreCommentsPlaceholderBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemNoCommentPlaceholderBinding;
import ml.docilealligator.infinityforreddit.fragments.ViewPostDetailFragment;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int DIVIDER_NORMAL = 0;
    public static final int DIVIDER_PARENT = 1;

    private static final int VIEW_TYPE_FIRST_LOADING = 9;
    private static final int VIEW_TYPE_FIRST_LOADING_FAILED = 10;
    private static final int VIEW_TYPE_NO_COMMENT_PLACEHOLDER = 11;
    private static final int VIEW_TYPE_COMMENT = 12;
    private static final int VIEW_TYPE_COMMENT_FULLY_COLLAPSED = 13;
    private static final int VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS = 14;
    private static final int VIEW_TYPE_IS_LOADING_MORE_COMMENTS = 15;
    private static final int VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED = 16;
    private static final int VIEW_TYPE_VIEW_ALL_COMMENTS = 17;

    private BaseActivity mActivity;
    private ViewPostDetailFragment mFragment;
    private Executor mExecutor;
    private Retrofit mRetrofit;
    private Retrofit mGQLRetrofit;
    private Retrofit mOauthRetrofit;
    private Markwon mCommentMarkwon;
    private String mAccessToken;
    private String mAccountName;
    private Post mPost;
    private ArrayList<Comment> mVisibleComments;
    private Locale mLocale;
    private RequestManager mGlide;
    private RecyclerView.RecycledViewPool recycledViewPool;
    private String mSingleCommentId;
    private boolean mIsSingleCommentThreadMode;
    private boolean mVoteButtonsOnTheRight;
    private boolean mShowElapsedTime;
    private String mTimeFormatPattern;
    private boolean mExpandChildren;
    private boolean mCommentToolbarHidden;
    private boolean mCommentToolbarHideOnClick;
    private boolean mSwapTapAndLong;
    private boolean mShowCommentDivider;
    private int mDividerType;
    private boolean mShowAbsoluteNumberOfVotes;
    private boolean mFullyCollapseComment;
    private boolean mShowOnlyOneCommentLevelIndicator;
    private boolean mHideCommentAwards;
    private boolean mShowAuthorAvatar;
    private boolean mAlwaysShowChildCommentCount;
    private boolean mHideTheNumberOfVotes;
    private int mDepthThreshold;
    private CommentRecyclerViewAdapterCallback mCommentRecyclerViewAdapterCallback;
    private boolean isInitiallyLoading;
    private boolean isInitiallyLoadingFailed;
    private boolean mHasMoreComments;
    private boolean loadMoreCommentsFailed;
    private Drawable expandDrawable;
    private Drawable collapseDrawable;

    private int mColorPrimaryLightTheme;
    private int mColorAccent;
    private int mCircularProgressBarBackgroundColor;
    private int mSecondaryTextColor;
    private int mPrimaryTextColor;
    private int mCommentTextColor;
    private int mCommentBackgroundColor;
    private int mDividerColor;
    private int mUsernameColor;
    private int mSubmitterColor;
    private int mModeratorColor;
    private int mDeletedColor;
    private int mCurrentUserColor;
    private int mAuthorFlairTextColor;
    private int mUpvotedColor;
    private int mDownvotedColor;
    private int mSingleCommentThreadBackgroundColor;
    private int mVoteAndReplyUnavailableVoteButtonColor;
    private int mButtonTextColor;
    private int mPostIconAndInfoColor;
    private int mCommentIconAndInfoColor;
    private int mFullyCollapsedCommentBackgroundColor;
    private int mAwardedCommentBackgroundColor;
    private int[] verticalBlockColors;

    private int mSearchCommentIndex = -1;

    public CommentsRecyclerViewAdapter(BaseActivity activity, ViewPostDetailFragment fragment,
                                       CustomThemeWrapper customThemeWrapper,
                                       Executor executor, Retrofit retrofit, Retrofit oauthRetrofit, Retrofit gqlRetrofit,
                                       String accessToken, String accountName,
                                       Post post, Locale locale, String singleCommentId,
                                       boolean isSingleCommentThreadMode,
                                       SharedPreferences sharedPreferences,
                                       CommentRecyclerViewAdapterCallback commentRecyclerViewAdapterCallback) {
        mActivity = activity;
        mFragment = fragment;
        mExecutor = executor;
        mRetrofit = retrofit;
        mOauthRetrofit = oauthRetrofit;
        mGQLRetrofit = gqlRetrofit;
        mGlide = Glide.with(activity);
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        mCommentTextColor = customThemeWrapper.getCommentColor();
        int commentSpoilerBackgroundColor = mCommentTextColor | 0xFF000000;
        int linkColor = customThemeWrapper.getLinkColor();
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (mActivity.contentTypeface != null) {
                    textView.setTypeface(mActivity.contentTypeface);
                }
                textView.setTextColor(mCommentTextColor);
                textView.setHighlightColor(Color.TRANSPARENT);
            }

            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost.isNSFW());
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
        mCommentMarkwon = MarkdownUtils.createFullRedditMarkwon(mActivity,
                miscPlugin, mCommentTextColor, commentSpoilerBackgroundColor, onLinkLongClickListener);
        recycledViewPool = new RecyclerView.RecycledViewPool();
        mAccessToken = accessToken;
        mAccountName = accountName;
        mPost = post;
        mVisibleComments = new ArrayList<>();
        mLocale = locale;
        mSingleCommentId = singleCommentId;
        mIsSingleCommentThreadMode = isSingleCommentThreadMode;

        mVoteButtonsOnTheRight = sharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
        mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
        mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
        mExpandChildren = !sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_TOP_LEVEL_COMMENTS_FIRST, false);
        mCommentToolbarHidden = sharedPreferences.getBoolean(SharedPreferencesUtils.COMMENT_TOOLBAR_HIDDEN, false);
        mCommentToolbarHideOnClick = sharedPreferences.getBoolean(SharedPreferencesUtils.COMMENT_TOOLBAR_HIDE_ON_CLICK, true);
        mSwapTapAndLong = sharedPreferences.getBoolean(SharedPreferencesUtils.SWAP_TAP_AND_LONG_COMMENTS, false);
        mShowCommentDivider = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_COMMENT_DIVIDER, false);
        mDividerType = Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.COMMENT_DIVIDER_TYPE, "0"));
        mShowAbsoluteNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES, true);
        mFullyCollapseComment = sharedPreferences.getBoolean(SharedPreferencesUtils.FULLY_COLLAPSE_COMMENT, false);
        mShowOnlyOneCommentLevelIndicator = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ONLY_ONE_COMMENT_LEVEL_INDICATOR, false);
        mHideCommentAwards = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_COMMENT_AWARDS, false);
        mShowAuthorAvatar = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_AUTHOR_AVATAR, false);
        mAlwaysShowChildCommentCount = sharedPreferences.getBoolean(SharedPreferencesUtils.ALWAYS_SHOW_CHILD_COMMENT_COUNT, false);
        mHideTheNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_VOTES_IN_COMMENTS, false);
        mDepthThreshold = sharedPreferences.getInt(SharedPreferencesUtils.SHOW_FEWER_TOOLBAR_OPTIONS_THRESHOLD, 5);

        mCommentRecyclerViewAdapterCallback = commentRecyclerViewAdapterCallback;
        isInitiallyLoading = true;
        isInitiallyLoadingFailed = false;
        mHasMoreComments = false;
        loadMoreCommentsFailed = false;

        expandDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_expand_more_grey_24dp, customThemeWrapper.getCommentIconAndInfoColor());
        collapseDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_expand_less_grey_24dp, customThemeWrapper.getCommentIconAndInfoColor());

        mColorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        mColorAccent = customThemeWrapper.getColorAccent();
        mCircularProgressBarBackgroundColor = customThemeWrapper.getCircularProgressBarBackground();
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mDividerColor = customThemeWrapper.getDividerColor();
        mCommentBackgroundColor = customThemeWrapper.getCommentBackgroundColor();
        mSubmitterColor = customThemeWrapper.getSubmitter();
        mModeratorColor = customThemeWrapper.getModerator();
        mDeletedColor = customThemeWrapper.getDeleted();
        mCurrentUserColor = customThemeWrapper.getCurrentUser();
        mAuthorFlairTextColor = customThemeWrapper.getAuthorFlairTextColor();
        mUsernameColor = customThemeWrapper.getUsername();
        mUpvotedColor = customThemeWrapper.getUpvoted();
        mDownvotedColor = customThemeWrapper.getDownvoted();
        mSingleCommentThreadBackgroundColor = customThemeWrapper.getSingleCommentThreadBackgroundColor();
        mVoteAndReplyUnavailableVoteButtonColor = customThemeWrapper.getVoteAndReplyUnavailableButtonColor();
        mButtonTextColor = customThemeWrapper.getButtonTextColor();
        mPostIconAndInfoColor = customThemeWrapper.getPostIconAndInfoColor();
        mCommentIconAndInfoColor = customThemeWrapper.getCommentIconAndInfoColor();
        mFullyCollapsedCommentBackgroundColor = customThemeWrapper.getFullyCollapsedCommentBackgroundColor();
        mAwardedCommentBackgroundColor = customThemeWrapper.getAwardedCommentBackgroundColor();

        verticalBlockColors = new int[] {
                customThemeWrapper.getCommentVerticalBarColor1(),
                customThemeWrapper.getCommentVerticalBarColor2(),
                customThemeWrapper.getCommentVerticalBarColor3(),
                customThemeWrapper.getCommentVerticalBarColor4(),
                customThemeWrapper.getCommentVerticalBarColor5(),
                customThemeWrapper.getCommentVerticalBarColor6(),
                customThemeWrapper.getCommentVerticalBarColor7(),
        };
    }

    @Override
    public int getItemViewType(int position) {
        if (mVisibleComments.size() == 0) {
            if (isInitiallyLoading) {
                return VIEW_TYPE_FIRST_LOADING;
            } else if (isInitiallyLoadingFailed) {
                return VIEW_TYPE_FIRST_LOADING_FAILED;
            } else {
                return VIEW_TYPE_NO_COMMENT_PLACEHOLDER;
            }
        }

        if (mIsSingleCommentThreadMode) {
            if (position == 0) {
                return VIEW_TYPE_VIEW_ALL_COMMENTS;
            }

            if (position == mVisibleComments.size() + 1) {
                if (mHasMoreComments) {
                    return VIEW_TYPE_IS_LOADING_MORE_COMMENTS;
                } else {
                    return VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED;
                }
            }

            Comment comment = mVisibleComments.get(position - 1);
            if (comment.getPlaceholderType() == Comment.NOT_PLACEHOLDER) {
                if (mFullyCollapseComment && !comment.isExpanded() && comment.hasExpandedBefore()) {
                    return VIEW_TYPE_COMMENT_FULLY_COLLAPSED;
                }
                return VIEW_TYPE_COMMENT;
            } else {
                return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
            }
        } else {
            if (position == mVisibleComments.size()) {
                if (mHasMoreComments) {
                    return VIEW_TYPE_IS_LOADING_MORE_COMMENTS;
                } else {
                    return VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED;
                }
            }

            Comment comment = mVisibleComments.get(position);
            if (comment.getPlaceholderType() == Comment.NOT_PLACEHOLDER) {
                if (mFullyCollapseComment && !comment.isExpanded() && comment.hasExpandedBefore()) {
                    return VIEW_TYPE_COMMENT_FULLY_COLLAPSED;
                }
                return VIEW_TYPE_COMMENT;
            } else {
                return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_FIRST_LOADING:
                return new LoadCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_comments, parent, false));
            case VIEW_TYPE_FIRST_LOADING_FAILED:
                return new LoadCommentsFailedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_comments_failed_placeholder, parent, false));
            case VIEW_TYPE_NO_COMMENT_PLACEHOLDER:
                return new NoCommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_no_comment_placeholder, parent, false));
            case VIEW_TYPE_COMMENT:
                return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
            case VIEW_TYPE_COMMENT_FULLY_COLLAPSED:
                return new CommentFullyCollapsedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_fully_collapsed, parent, false));
            case VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS:
                return new LoadMoreChildCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more_comments_placeholder, parent, false));
            case VIEW_TYPE_IS_LOADING_MORE_COMMENTS:
                return new IsLoadingMoreCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_footer_loading, parent, false));
            case VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED:
                return new LoadMoreCommentsFailedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_footer_error, parent, false));
            default:
                return new ViewAllCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_all_comments, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            Comment comment = getCurrentComment(position);
            if (comment != null) {
                if (mIsSingleCommentThreadMode && comment.getId().equals(mSingleCommentId)) {
                    holder.itemView.setBackgroundColor(mSingleCommentThreadBackgroundColor);
                } else if (comment.getAwards() != null && !comment.getAwards().equals("")) {
                    holder.itemView.setBackgroundColor(mAwardedCommentBackgroundColor);
                }

                String authorPrefixed = "u/" + comment.getAuthor();
                ((CommentViewHolder) holder).binding.authorTextView.setText(authorPrefixed);

                if (comment.getAuthorFlairHTML() != null && !comment.getAuthorFlairHTML().equals("")) {
                    ((CommentViewHolder) holder).binding.authorFlairTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((CommentViewHolder) holder).binding.authorFlairTextView, comment.getAuthorFlairHTML(), true);
                } else if (comment.getAuthorFlair() != null && !comment.getAuthorFlair().equals("")) {
                    ((CommentViewHolder) holder).binding.authorFlairTextView.setVisibility(View.VISIBLE);
                    ((CommentViewHolder) holder).binding.authorFlairTextView.setText(comment.getAuthorFlair());
                }

                if (comment.isSubmitter()) {
                    ((CommentViewHolder) holder).binding.authorTextView.setTextColor(mSubmitterColor);
                    Drawable submitterDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_mic_14dp, mSubmitterColor);
                    ((CommentViewHolder) holder).binding.authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                            submitterDrawable, null, null, null);
                } else if (comment.isModerator()) {
                    ((CommentViewHolder) holder).binding.authorTextView.setTextColor(mModeratorColor);
                    Drawable moderatorDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_verified_user_14dp, mModeratorColor);
                    ((CommentViewHolder) holder).binding.authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                            moderatorDrawable, null, null, null);
                } else if (comment.getAuthor().equals(mAccountName)) {
                    ((CommentViewHolder) holder).binding.authorTextView.setTextColor(mCurrentUserColor);
                    Drawable currentUserDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_current_user_14dp, mCurrentUserColor);
                    ((CommentViewHolder) holder).binding.authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                            currentUserDrawable, null, null, null);
                }else if (comment.isAuthorDeleted()) {
                    ((CommentViewHolder) holder).binding.authorTextView.setTextColor(mDeletedColor);
                }

                if (comment.getAuthorIconUrl() == null) {
                    mFragment.loadIcon(comment.getAuthor(), (authorName, iconUrl) -> {
                        if (authorName.equals(comment.getAuthor())) {
                            comment.setAuthorIconUrl(iconUrl);
                        }

                        Comment currentComment = getCurrentComment(holder);
                        if (currentComment != null && authorName.equals(currentComment.getAuthor())) {
                            mGlide.load(iconUrl)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((CommentViewHolder) holder).binding.authorIconImageView);
                        }
                    });
                } else {
                    mGlide.load(comment.getAuthorIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((CommentViewHolder) holder).binding.authorIconImageView);
                }

                if (mShowElapsedTime) {
                    ((CommentViewHolder) holder).binding.commentTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
                } else {
                    ((CommentViewHolder) holder).binding.commentTimeTextView.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
                }

                if (mCommentToolbarHidden) {
                    ((CommentViewHolder) holder).binding.bottomConstraintLayout.getLayoutParams().height = 0;
                    if (!mHideTheNumberOfVotes) {
                        ((CommentViewHolder) holder).binding.topScoreTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    ((CommentViewHolder) holder).binding.bottomConstraintLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    ((CommentViewHolder) holder).binding.topScoreTextView.setVisibility(View.GONE);
                }

                if (!mHideCommentAwards && comment.getAwards() != null && !comment.getAwards().equals("")) {
                    ((CommentViewHolder) holder).binding.awardsTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((CommentViewHolder) holder).binding.awardsTextView, comment.getAwards(), true);
                }

                ((CommentViewHolder) holder).mMarkwonAdapter.setMarkdown(mCommentMarkwon, comment.getCommentMarkdown());
                // noinspection NotifyDataSetChanged
                ((CommentViewHolder) holder).mMarkwonAdapter.notifyDataSetChanged();

                if (!mHideTheNumberOfVotes) {
                    String commentText = "";
                    String topScoreText = "";
                    if (comment.isScoreHidden()) {
                        commentText = mActivity.getString(R.string.hidden);
                    } else {
                        commentText = Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType());
                        topScoreText = mActivity.getString(R.string.top_score,
                                Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                        comment.getScore() + comment.getVoteType()));
                    }
                    ((CommentViewHolder) holder).binding.scoreTextView.setText(commentText);
                    ((CommentViewHolder) holder).binding.topScoreTextView.setText(topScoreText);
                } else {
                    ((CommentViewHolder) holder).binding.scoreTextView.setText(mActivity.getString(R.string.vote));
                }

                if (comment.isEdited()) {
                    ((CommentViewHolder) holder).binding.editedTextView.setVisibility(View.VISIBLE);
                } else {
                    ((CommentViewHolder) holder).binding.editedTextView.setVisibility(View.GONE);
                }

                ((CommentViewHolder) holder).binding.commentIndentationView.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
                ((CommentViewHolder) holder).binding.commentIndentationView.setLevelAndColors(comment.getDepth(), verticalBlockColors);
                if (comment.getDepth() >= mDepthThreshold) {
                    ((CommentViewHolder) holder).binding.saveButton.setVisibility(View.GONE);
                    ((CommentViewHolder) holder).binding.replyButton.setVisibility(View.GONE);
                } else {
                    ((CommentViewHolder) holder).binding.saveButton.setVisibility(View.VISIBLE);
                    ((CommentViewHolder) holder).binding.replyButton.setVisibility(View.VISIBLE);
                }

                if (comment.hasReply()) {
                    if (comment.getChildCount() > 0 && (mAlwaysShowChildCommentCount || !comment.isExpanded())) {
                        ((CommentViewHolder) holder).binding.expandButton.setText("+" + comment.getChildCount());
                    }
                    if (comment.isExpanded()) {
                        ((CommentViewHolder) holder).binding.expandButton.setCompoundDrawablesWithIntrinsicBounds(collapseDrawable, null, null, null);
                    } else {
                        ((CommentViewHolder) holder).binding.expandButton.setCompoundDrawablesWithIntrinsicBounds(expandDrawable, null, null, null);
                    }
                    ((CommentViewHolder) holder).binding.expandButton.setVisibility(View.VISIBLE);
                }

                switch (comment.getVoteType()) {
                    case Comment.VOTE_TYPE_UPVOTE:
                        ((CommentViewHolder) holder).binding.upvoteButton
                                .setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).binding.scoreTextView.setTextColor(mUpvotedColor);
                        ((CommentViewHolder) holder).binding.topScoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case Comment.VOTE_TYPE_DOWNVOTE:
                        ((CommentViewHolder) holder).binding.downvoteButton
                                .setColorFilter(mDownvotedColor, PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).binding.scoreTextView.setTextColor(mDownvotedColor);
                        ((CommentViewHolder) holder).binding.topScoreTextView.setTextColor(mDownvotedColor);
                        break;
                }

                if (mPost.isArchived()) {
                    ((CommentViewHolder) holder).binding.replyButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                    PorterDuff.Mode.SRC_IN);
                    ((CommentViewHolder) holder).binding.upvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                    PorterDuff.Mode.SRC_IN);
                    ((CommentViewHolder) holder).binding.downvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                    PorterDuff.Mode.SRC_IN);
                }

                if (mPost.isLocked()) {
                    ((CommentViewHolder) holder).binding.replyButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                    PorterDuff.Mode.SRC_IN);
                }

                if (comment.isSaved()) {
                    ((CommentViewHolder) holder).binding.saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((CommentViewHolder) holder).binding.saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                if (position == mSearchCommentIndex) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#03A9F4"));
                }

                if (mShowCommentDivider) {
                    if (mDividerType == DIVIDER_PARENT && comment.getDepth() == 0) {
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                        params.setMargins(0, (int) Utils.convertDpToPixel(16, mActivity), 0, 0);
                    }
                }
            }
        } else if (holder instanceof CommentFullyCollapsedViewHolder) {
            Comment comment = getCurrentComment(position);
            if (comment != null) {
                String authorWithPrefix = "u/" + comment.getAuthor();
                ((CommentFullyCollapsedViewHolder) holder).binding.userNameTextView.setText(authorWithPrefix);

                if (comment.getAuthorIconUrl() == null) {
                    mFragment.loadIcon(comment.getAuthor(), (authorName, iconUrl) -> {
                        if (authorName.equals(comment.getAuthor())) {
                            comment.setAuthorIconUrl(iconUrl);
                        }

                        Comment currentComment = getCurrentComment(holder);
                        if (currentComment != null && authorName.equals(currentComment.getAuthor())) {
                            mGlide.load(iconUrl)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((CommentFullyCollapsedViewHolder) holder).binding.authorIconImageView);
                        }
                    });
                } else {
                    mGlide.load(comment.getAuthorIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((CommentFullyCollapsedViewHolder) holder).binding.authorIconImageView);
                }

                if (comment.getChildCount() > 0) {
                    ((CommentFullyCollapsedViewHolder) holder).binding.childCountTextView.setVisibility(View.VISIBLE);
                    ((CommentFullyCollapsedViewHolder) holder).binding.childCountTextView.setText("+" + comment.getChildCount());
                } else {
                    ((CommentFullyCollapsedViewHolder) holder).binding.childCountTextView.setVisibility(View.GONE);
                }
                if (mShowElapsedTime) {
                    ((CommentFullyCollapsedViewHolder) holder).binding.timeTextView.setText(Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
                } else {
                    ((CommentFullyCollapsedViewHolder) holder).binding.timeTextView.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
                }
                if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                    ((CommentFullyCollapsedViewHolder) holder).binding.scoreTextView.setText(mActivity.getString(R.string.top_score,
                            Utils.getNVotes(mShowAbsoluteNumberOfVotes, comment.getScore() + comment.getVoteType())));
                } else if (mHideTheNumberOfVotes) {
                    ((CommentFullyCollapsedViewHolder) holder).binding.scoreTextView.setText(mActivity.getString(R.string.vote));
                } else {
                    ((CommentFullyCollapsedViewHolder) holder).binding.scoreTextView.setText(mActivity.getString(R.string.hidden));
                }
                ((CommentFullyCollapsedViewHolder) holder).binding.commentIndentationView.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
                ((CommentFullyCollapsedViewHolder) holder).binding.commentIndentationView.setLevelAndColors(comment.getDepth(), verticalBlockColors);

                if (mShowCommentDivider) {
                    if (mDividerType == DIVIDER_PARENT && comment.getDepth() == 0) {
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                        params.setMargins(0, (int) Utils.convertDpToPixel(16, mActivity), 0, 0);
                    }
                }
            }
        } else if (holder instanceof LoadMoreChildCommentsViewHolder) {
            Comment placeholder;
            placeholder = mIsSingleCommentThreadMode ? mVisibleComments.get(holder.getBindingAdapterPosition() - 1)
                    : mVisibleComments.get(holder.getBindingAdapterPosition());

            ((LoadMoreChildCommentsViewHolder) holder).binding.commentIndentationView.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
            ((LoadMoreChildCommentsViewHolder) holder).binding.commentIndentationView.setLevelAndColors(placeholder.getDepth(), verticalBlockColors);

            if (placeholder.getPlaceholderType() == Comment.PLACEHOLDER_LOAD_MORE_COMMENTS) {
                if (placeholder.isLoadingMoreChildren()) {
                    ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setText(R.string.loading);
                } else if (placeholder.isLoadMoreChildrenFailed()) {
                    ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                } else {
                    ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setText(R.string.comment_load_more_comments);
                }
            } else {
                ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setText(R.string.comment_continue_thread);
            }

            if (placeholder.getPlaceholderType() == Comment.PLACEHOLDER_LOAD_MORE_COMMENTS) {
                ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setOnClickListener(view -> {
                    int commentPosition = mIsSingleCommentThreadMode ? holder.getBindingAdapterPosition() - 1 : holder.getBindingAdapterPosition();
                    int parentPosition = getParentPosition(commentPosition);
                    if (parentPosition >= 0) {
                        Comment parentComment = mVisibleComments.get(parentPosition);

                        mVisibleComments.get(commentPosition).setLoadingMoreChildren(true);
                        mVisibleComments.get(commentPosition).setLoadMoreChildrenFailed(false);
                        ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setText(R.string.loading);

                        Retrofit retrofit = mAccessToken == null ? mRetrofit : mGQLRetrofit;
                        SortType.Type sortType = mCommentRecyclerViewAdapterCallback.getSortType();
                        FetchComment.fetchMoreComment(mExecutor, new Handler(), retrofit, mAccessToken,
                                parentComment.getMoreChildrenIds(),
                                mExpandChildren, mPost.getFullName(), sortType, mPost.getAuthor(), new FetchComment.FetchMoreCommentListener() {
                                    @Override
                                    public void onFetchMoreCommentSuccess(ArrayList<Comment> topLevelComments,
                                                                          ArrayList<Comment> expandedComments,
                                                                          ArrayList<String> moreChildrenIds) {
                                        if (mVisibleComments.size() > parentPosition
                                                && parentComment.getFullName().equals(mVisibleComments.get(parentPosition).getFullName())) {
                                            if (mVisibleComments.get(parentPosition).isExpanded()) {
                                                if (!moreChildrenIds.isEmpty()) {
                                                    mVisibleComments.get(parentPosition).setMoreChildrenIds(moreChildrenIds);
                                                    mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                            .setLoadingMoreChildren(false);
                                                    mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                            .setLoadMoreChildrenFailed(false);

                                                    int placeholderPosition = findLoadMoreCommentsPlaceholderPosition(parentComment.getFullName(), commentPosition);
                                                    if (placeholderPosition != -1) {
                                                        mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                        mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(false);
                                                        ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setText(R.string.comment_load_more_comments);

                                                        mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                        if (mIsSingleCommentThreadMode) {
                                                            notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                        } else {
                                                            notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                                        }
                                                    }
                                                } else {
                                                    mVisibleComments.get(parentPosition).getChildren()
                                                            .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                                    mVisibleComments.get(parentPosition).removeMoreChildrenIds();

                                                    int placeholderPosition = findLoadMoreCommentsPlaceholderPosition(parentComment.getFullName(), commentPosition);
                                                    if (placeholderPosition != -1) {
                                                        mVisibleComments.remove(placeholderPosition);
                                                        if (mIsSingleCommentThreadMode) {
                                                            notifyItemRemoved(placeholderPosition + 1);
                                                        } else {
                                                            notifyItemRemoved(placeholderPosition);
                                                        }

                                                        mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                        if (mIsSingleCommentThreadMode) {
                                                            notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                        } else {
                                                            notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (mVisibleComments.get(parentPosition).hasReply() && moreChildrenIds.isEmpty()) {
                                                    mVisibleComments.get(parentPosition).getChildren()
                                                            .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                                    mVisibleComments.get(parentPosition).removeMoreChildrenIds();
                                                }
                                            }

                                            mVisibleComments.get(parentPosition).addChildren(topLevelComments);
                                            if (mIsSingleCommentThreadMode) {
                                                notifyItemChanged(parentPosition + 1);
                                            } else {
                                                notifyItemChanged(parentPosition);
                                            }
                                        } else {
                                            for (int i = 0; i < mVisibleComments.size(); i++) {
                                                if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                    if (mVisibleComments.get(i).isExpanded()) {
                                                        int placeholderPositionHint = i + mVisibleComments.get(i).getChildren().size();
                                                        int placeholderPosition = findLoadMoreCommentsPlaceholderPosition(parentComment.getFullName(), placeholderPositionHint);

                                                        if (placeholderPosition != -1) {
                                                            mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                            mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(false);
                                                            ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setText(R.string.comment_load_more_comments);

                                                            mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                            if (mIsSingleCommentThreadMode) {
                                                                notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                            } else {
                                                                notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                                            }
                                                        }
                                                    }

                                                    mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                            .setLoadingMoreChildren(false);
                                                    mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                            .setLoadMoreChildrenFailed(false);
                                                    mVisibleComments.get(i).addChildren(topLevelComments);
                                                    if (mIsSingleCommentThreadMode) {
                                                        notifyItemChanged(i + 1);
                                                    } else {
                                                        notifyItemChanged(i);
                                                    }

                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFetchMoreCommentFailed() {
                                        int currentParentPosition = findCommentPosition(parentComment.getFullName(), parentPosition);
                                        if (currentParentPosition == -1) {
                                            // note: returning here is probably a mistake, because
                                            // parent is just not visible, but it can still exist in the comments tree.
                                            return;
                                        }
                                        Comment currentParentComment = mVisibleComments.get(currentParentPosition);

                                        if (currentParentComment.isExpanded()) {
                                            int placeholderPositionHint = currentParentPosition + currentParentComment.getChildren().size();
                                            int placeholderPosition = findLoadMoreCommentsPlaceholderPosition(parentComment.getFullName(), placeholderPositionHint);

                                            if (placeholderPosition != -1) {
                                                mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(true);
                                            }
                                            ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                                        }
                                        currentParentComment.getChildren().get(currentParentComment.getChildren().size() - 1)
                                                .setLoadingMoreChildren(false);
                                        currentParentComment.getChildren().get(currentParentComment.getChildren().size() - 1)
                                                .setLoadMoreChildrenFailed(true);
                                    }
                                });
                    }
                });
            } else {
                ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextView.setOnClickListener(view -> {
                    Comment comment = getCurrentComment(holder);
                    if (comment != null) {
                        Intent intent = new Intent(mActivity, ViewPostDetailActivity.class);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, mPost);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, comment.getParentId());
                        intent.putExtra(ViewPostDetailActivity.EXTRA_CONTEXT_NUMBER, "0");
                        mActivity.startActivity(intent);
                    }
                });
            }
        }
    }

    private int getParentPosition(int position) {
        if (position >= 0 && position < mVisibleComments.size()) {
            int childDepth = mVisibleComments.get(position).getDepth();
            for (int i = position; i >= 0; i--) {
                if (mVisibleComments.get(i).getDepth() < childDepth) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Find position of comment with given {@code fullName} and
     * {@link Comment#NOT_PLACEHOLDER} placeholder type
     * @return position of the placeholder or -1 if not found
     */
    private int findCommentPosition(String fullName, int positionHint) {
        return findCommentPosition(fullName, positionHint, Comment.NOT_PLACEHOLDER);
    }

    /**
     * Find position of comment with given {@code fullName} and
     * {@link Comment#PLACEHOLDER_LOAD_MORE_COMMENTS} placeholder type
     * @return position of the placeholder or -1 if not found
     */
    private int findLoadMoreCommentsPlaceholderPosition(String fullName, int positionHint) {
        return findCommentPosition(fullName, positionHint, Comment.PLACEHOLDER_LOAD_MORE_COMMENTS);
    }

    private int findCommentPosition(String fullName, int positionHint, int placeholderType) {
        if (0 <= positionHint && positionHint < mVisibleComments.size()
                && mVisibleComments.get(positionHint).getFullName().equals(fullName)
                && mVisibleComments.get(positionHint).getPlaceholderType() == placeholderType) {
            return positionHint;
        }

        for (int i = 0; i < mVisibleComments.size(); i++) {
            Comment comment = mVisibleComments.get(i);
            if (comment.getFullName().equals(fullName) && comment.getPlaceholderType() == placeholderType) {
                return i;
            }
        }
        return -1;
    }

    private void expandChildren(ArrayList<Comment> comments, ArrayList<Comment> newList) {
        if (comments != null && comments.size() > 0) {
            for (Comment comment : comments) {
                newList.add(comment);
                expandChildren(comment.getChildren(), newList);
                comment.setExpanded(true);
            }
        }
    }

    private void collapseChildren(int position) {
        mVisibleComments.get(position).setExpanded(false);
        int depth = mVisibleComments.get(position).getDepth();
        int allChildrenSize = 0;
        for (int i = position + 1; i < mVisibleComments.size(); i++) {
            if (mVisibleComments.get(i).getDepth() > depth) {
                allChildrenSize++;
            } else {
                break;
            }
        }

        if (allChildrenSize > 0) {
            mVisibleComments.subList(position + 1, position + 1 + allChildrenSize).clear();
        }
        if (mIsSingleCommentThreadMode) {
            notifyItemRangeRemoved(position + 2, allChildrenSize);
            if (mFullyCollapseComment) {
                notifyItemChanged(position + 1);
            }
        } else {
            notifyItemRangeRemoved(position + 1, allChildrenSize);
            if (mFullyCollapseComment) {
                notifyItemChanged(position);
            }
        }
    }

    public void addComments(@NonNull ArrayList<Comment> comments, boolean hasMoreComments) {
        if (mVisibleComments.size() == 0) {
            isInitiallyLoading = false;
            isInitiallyLoadingFailed = false;
            if (comments.size() == 0) {
                notifyItemChanged(0);
            } else {
                notifyItemRemoved(0);
            }
        }

        int sizeBefore = mVisibleComments.size();
        mVisibleComments.addAll(comments);
        if (mIsSingleCommentThreadMode) {
            notifyItemRangeInserted(sizeBefore, comments.size() + 1);
        } else {
            notifyItemRangeInserted(sizeBefore, comments.size());
        }

        if (mHasMoreComments != hasMoreComments) {
            if (hasMoreComments) {
                if (mIsSingleCommentThreadMode) {
                    notifyItemInserted(mVisibleComments.size() + 1);
                } else {
                    notifyItemInserted(mVisibleComments.size());
                }
            } else {
                if (mIsSingleCommentThreadMode) {
                    notifyItemRemoved(mVisibleComments.size() + 1);
                } else {
                    notifyItemRemoved(mVisibleComments.size());
                }
            }
        }
        mHasMoreComments = hasMoreComments;
    }

    public void addComment(Comment comment) {
        if (mVisibleComments.size() == 0 || isInitiallyLoadingFailed) {
            notifyItemRemoved(1);
        }

        mVisibleComments.add(0, comment);

        if (isInitiallyLoading) {
            notifyItemInserted(1);
        } else {
            notifyItemInserted(0);
        }
    }

    public void addChildComment(Comment comment, String parentFullname, int parentPosition) {
        if (!parentFullname.equals(mVisibleComments.get(parentPosition).getFullName())) {
            for (int i = 0; i < mVisibleComments.size(); i++) {
                if (parentFullname.equals(mVisibleComments.get(i).getFullName())) {
                    parentPosition = i;
                    break;
                }
            }
        }

        mVisibleComments.get(parentPosition).addChild(comment);
        mVisibleComments.get(parentPosition).setHasReply(true);
        if (!mVisibleComments.get(parentPosition).isExpanded()) {
            ArrayList<Comment> newList = new ArrayList<>();
            expandChildren(mVisibleComments.get(parentPosition).getChildren(), newList);
            mVisibleComments.get(parentPosition).setExpanded(true);
            mVisibleComments.addAll(parentPosition + 1, newList);
            if (mIsSingleCommentThreadMode) {
                notifyItemChanged(parentPosition + 1);
                notifyItemRangeInserted(parentPosition + 2, newList.size());
            } else {
                notifyItemChanged(parentPosition);
                notifyItemRangeInserted(parentPosition + 1, newList.size());
            }
        } else {
            mVisibleComments.add(parentPosition + 1, comment);
            if (mIsSingleCommentThreadMode) {
                notifyItemChanged(parentPosition + 1);
                notifyItemInserted(parentPosition + 2);
            } else {
                notifyItemChanged(parentPosition);
                notifyItemInserted(parentPosition + 1);
            }
        }
    }

    public void setSingleComment(String singleCommentId, boolean isSingleCommentThreadMode) {
        mSingleCommentId = singleCommentId;
        mIsSingleCommentThreadMode = isSingleCommentThreadMode;
    }

    public ArrayList<Comment> getVisibleComments() {
        return mVisibleComments;
    }

    public void initiallyLoading() {
        resetCommentSearchIndex();
        int removedItemCount = getItemCount();
        mVisibleComments.clear();
        notifyItemRangeRemoved(0, removedItemCount);
        isInitiallyLoading = true;
        isInitiallyLoadingFailed = false;
        notifyItemInserted(0);
    }

    public void initiallyLoadCommentsFailed() {
        isInitiallyLoading = false;
        isInitiallyLoadingFailed = true;
        notifyItemChanged(0);
    }

    public void loadMoreCommentsFailed() {
        loadMoreCommentsFailed = true;
        if (mIsSingleCommentThreadMode) {
            notifyItemChanged(mVisibleComments.size() + 1);
        } else {
            notifyItemChanged(mVisibleComments.size());
        }
    }

    public void editComment(String commentAuthor, String commentContentMarkdown, int position) {
        if (commentAuthor != null) {
            mVisibleComments.get(position).setAuthor(commentAuthor);
        }

        mVisibleComments.get(position).setSubmittedByAuthor(mVisibleComments.get(position).isSubmitter());

        mVisibleComments.get(position).setCommentMarkdown(commentContentMarkdown);
        if (mIsSingleCommentThreadMode) {
            notifyItemChanged(position + 1);
        } else {
            notifyItemChanged(position);
        }
    }

    public void editComment(Comment fetchedComment, Comment originalComment, int position) {
        if (position >= mVisibleComments.size() || !mVisibleComments.get(position).equals(originalComment)) {
            position = mVisibleComments.indexOf(originalComment);
            if (position < 0) {
                Toast.makeText(mActivity, R.string.show_removed_comment_failed, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mVisibleComments.get(position).setSubmittedByAuthor(originalComment.isSubmitter());
        mVisibleComments.get(position).setCommentMarkdown(fetchedComment.getCommentMarkdown());

        if (mIsSingleCommentThreadMode) {
            notifyItemChanged(position + 1);
        } else {
            notifyItemChanged(position);
        }
    }

    public void deleteComment(int position) {
        if (mVisibleComments != null && position >= 0 && position < mVisibleComments.size()) {
            if (mVisibleComments.get(position).hasReply()) {
                mVisibleComments.get(position).setAuthor("[deleted]");
                mVisibleComments.get(position).setCommentMarkdown("[deleted]");
                if (mIsSingleCommentThreadMode) {
                    notifyItemChanged(position + 1);
                } else {
                    notifyItemChanged(position);
                }
            } else {
                mVisibleComments.remove(position);
                if (mIsSingleCommentThreadMode) {
                    notifyItemRemoved(position + 1);
                } else {
                    notifyItemRemoved(position);
                }
            }
        }
    }

    public int getNextParentCommentPosition(int currentPosition) {
        if (mVisibleComments != null && !mVisibleComments.isEmpty()) {
            if (mIsSingleCommentThreadMode) {
                for (int i = currentPosition + 1; i - 1 < mVisibleComments.size() && i - 1 >= 0; i++) {
                    if (mVisibleComments.get(i - 1).getDepth() == 0) {
                        return i;
                    }
                }
            } else {
                for (int i = currentPosition + 1; i < mVisibleComments.size(); i++) {
                    if (mVisibleComments.get(i).getDepth() == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public int getPreviousParentCommentPosition(int currentPosition) {
        if (mVisibleComments != null && !mVisibleComments.isEmpty()) {
            if (mIsSingleCommentThreadMode) {
                for (int i = currentPosition - 1; i - 1 >= 0; i--) {
                    if (mVisibleComments.get(i - 1).getDepth() == 0) {
                        return i;
                    }
                }
            } else {
                for (int i = currentPosition - 1; i >= 0; i--) {
                    if (mVisibleComments.get(i).getDepth() == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
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

    public void giveAward(String awardsHTML, int awardCount, int position) {
        position = mIsSingleCommentThreadMode ? position + 1 : position;
        Comment comment = getCurrentComment(position);
        if (comment != null) {
            comment.addAwards(awardsHTML);
            notifyItemChanged(position);
        }
    }

    public void setSaveComment(int position, boolean isSaved) {
        Comment comment = getCurrentComment(position);
        if (comment != null) {
            comment.setSaved(isSaved);
        }
    }

    public int getSearchCommentIndex() {
        return mSearchCommentIndex;
    }

    public void highlightSearchResult(int searchCommentIndex) {
        mSearchCommentIndex = searchCommentIndex;
    }

    public void resetCommentSearchIndex() {
        mSearchCommentIndex = -1;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof CommentViewHolder) {
            holder.itemView.setBackgroundColor(mCommentBackgroundColor);
            ((CommentViewHolder) holder).binding.authorTextView.setTextColor(mUsernameColor);
            ((CommentViewHolder) holder).binding.authorFlairTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).binding.authorTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mGlide.clear(((CommentViewHolder) holder).binding.authorIconImageView);
            ((CommentViewHolder) holder).binding.topScoreTextView.setTextColor(mSecondaryTextColor);
            ((CommentViewHolder) holder).binding.awardsTextView.setText("");
            ((CommentViewHolder) holder).binding.awardsTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).binding.expandButton.setVisibility(View.GONE);
            ((CommentViewHolder) holder).binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
            ((CommentViewHolder) holder).binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).binding.expandButton.setText("");
            ((CommentViewHolder) holder).binding.replyButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        if (isInitiallyLoading || isInitiallyLoadingFailed || mVisibleComments.size() == 0) {
            return 1;
        }

        if (mHasMoreComments || loadMoreCommentsFailed) {
            if (mIsSingleCommentThreadMode) {
                return mVisibleComments.size() + 2;
            } else {
                return mVisibleComments.size() + 1;
            }
        }

        if (mIsSingleCommentThreadMode) {
            return mVisibleComments.size() + 1;
        } else {
            return mVisibleComments.size();
        }
    }

    public interface CommentRecyclerViewAdapterCallback {
        void retryFetchingComments();

        void retryFetchingMoreComments();

        SortType.Type getSortType();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;
        CustomMarkwonAdapter mMarkwonAdapter;

        CommentViewHolder(View itemView) {
            super(itemView);
            binding = ItemCommentBinding.bind(itemView);

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

            if (binding.linearLayout.getLayoutTransition() != null) {
                binding.linearLayout.getLayoutTransition().setAnimateParentHierarchy(false);
            }

            if (mShowCommentDivider) {
                if (mDividerType == DIVIDER_NORMAL) {
                    binding.divider.setBackgroundColor(mDividerColor);
                    binding.divider.setVisibility(View.VISIBLE);
                }
            }

            if (mActivity.typeface != null) {
                binding.authorTextView.setTypeface(mActivity.typeface);
                binding.commentTimeTextView.setTypeface(mActivity.typeface);
                binding.authorFlairTextView.setTypeface(mActivity.typeface);
                binding.topScoreTextView.setTypeface(mActivity.typeface);
                binding.editedTextView.setTypeface(mActivity.typeface);
                binding.awardsTextView.setTypeface(mActivity.typeface);
                binding.scoreTextView.setTypeface(mActivity.typeface);
                binding.expandButton.setTypeface(mActivity.typeface);
            }

            if (mShowAuthorAvatar) {
                binding.authorIconImageView.setVisibility(View.VISIBLE);
            } else {
                ((ConstraintLayout.LayoutParams) binding.authorTextView.getLayoutParams()).leftMargin = 0;
                ((ConstraintLayout.LayoutParams) binding.authorFlairTextView.getLayoutParams()).leftMargin = 0;
            }

            binding.commentMarkdownView.setRecycledViewPool(recycledViewPool);
            LinearLayoutManagerBugFixed linearLayoutManager = new SwipeLockLinearLayoutManager(mActivity, new SwipeLockInterface() {
                @Override
                public void lockSwipe() {
                    ((ViewPostDetailActivity) mActivity).lockSwipeRightToGoBack();
                }

                @Override
                public void unlockSwipe() {
                    ((ViewPostDetailActivity) mActivity).unlockSwipeRightToGoBack();
                }
            });
            binding.commentMarkdownView.setLayoutManager(linearLayoutManager);
            mMarkwonAdapter = MarkdownUtils.createCustomTablesAdapter();
            binding.commentMarkdownView.setAdapter(mMarkwonAdapter);

            itemView.setBackgroundColor(mCommentBackgroundColor);
            binding.authorTextView.setTextColor(mUsernameColor);
            binding.commentTimeTextView.setTextColor(mSecondaryTextColor);
            binding.authorFlairTextView.setTextColor(mAuthorFlairTextColor);
            binding.topScoreTextView.setTextColor(mSecondaryTextColor);
            binding.editedTextView.setTextColor(mSecondaryTextColor);
            binding.awardsTextView.setTextColor(mSecondaryTextColor);
            binding.divider.setBackgroundColor(mDividerColor);
            binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
            binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            binding.moreButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            binding.expandButton.setTextColor(mCommentIconAndInfoColor);
            binding.saveButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            binding.replyButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);

            binding.authorFlairTextView.setOnClickListener(view -> binding.authorTextView.performClick());

            binding.editedTextView.setOnClickListener(view -> {
                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.edited_time, mShowElapsedTime ?
                            Utils.getElapsedTime(mActivity, comment.getEditedTimeMillis()) :
                            Utils.getFormattedTime(mLocale, comment.getEditedTimeMillis(), mTimeFormatPattern)
                    ), Toast.LENGTH_SHORT).show();
                }
            });

            binding.moreButton.setOnClickListener(view -> {
                getItemCount();
                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    Bundle bundle = new Bundle();
                    if (!mPost.isArchived() && !mPost.isLocked() && comment.getAuthor().equals(mAccountName)) {
                        bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_EDIT_AND_DELETE_AVAILABLE, true);
                    }
                    bundle.putString(CommentMoreBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    bundle.putParcelable(CommentMoreBottomSheetFragment.EXTRA_COMMENT, comment);
                    if (mIsSingleCommentThreadMode) {
                        bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition() - 1);
                    } else {
                        bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition());
                    }
                    bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_IS_NSFW, mPost.isNSFW());
                    if (comment.getDepth() >= mDepthThreshold) {
                        bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_SHOW_REPLY_AND_SAVE_OPTION, true);
                    }
                    CommentMoreBottomSheetFragment commentMoreBottomSheetFragment = new CommentMoreBottomSheetFragment();
                    commentMoreBottomSheetFragment.setArguments(bundle);
                    commentMoreBottomSheetFragment.show(mActivity.getSupportFragmentManager(), commentMoreBottomSheetFragment.getTag());
                }
            });

            binding.replyButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_reply_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPost.isLocked()) {
                    Toast.makeText(mActivity, R.string.locked_post_reply_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    Intent intent = new Intent(mActivity, CommentActivity.class);
                    intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, comment.getDepth() + 1);
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, comment.getCommentMarkdown());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, comment.getCommentRawText());
                    intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, comment.getFullName());
                    intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, true);

                    int parentPosition = mIsSingleCommentThreadMode ? getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                    intent.putExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, parentPosition);
                    mFragment.startActivityForResult(intent, CommentActivity.WRITE_COMMENT_REQUEST_CODE);
                }
            });

            binding.upvoteButton.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != Comment.VOTE_TYPE_UPVOTE) {
                        //Not upvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                        newVoteType = APIUtils.DIR_UPVOTE;
                        binding.upvoteButton.setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                        binding.scoreTextView.setTextColor(mUpvotedColor);
                        binding.topScoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                        binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
                        binding.topScoreTextView.setTextColor(mSecondaryTextColor);
                    }

                    if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                        binding.scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                        binding.topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                        comment.getScore() + comment.getVoteType())));
                    }

                    VoteThing.voteThing(mActivity, mGQLRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                                if (currentPosition == position) {
                                    binding.upvoteButton.setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                                    binding.scoreTextView.setTextColor(mUpvotedColor);
                                    binding.topScoreTextView.setTextColor(mUpvotedColor);
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                    binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                    binding.topScoreTextView.setTextColor(mSecondaryTextColor);
                                }
                            }

                            if (currentPosition == position) {
                                binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                                    binding.scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                            comment.getScore() + comment.getVoteType()));
                                    binding.topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                            Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                                    comment.getScore() + comment.getVoteType())));
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
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getCurrentComment(this);
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
                        binding.topScoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                        binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
                        binding.topScoreTextView.setTextColor(mSecondaryTextColor);
                    }

                    if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                        binding.scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                        binding.topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                        comment.getScore() + comment.getVoteType())));
                    }

                    int position = getBindingAdapterPosition();
                    VoteThing.voteThing(mActivity, mGQLRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                                comment.setVoteType(Comment.VOTE_TYPE_DOWNVOTE);
                                if (currentPosition == position) {
                                    binding.downvoteButton.setColorFilter(mDownvotedColor, PorterDuff.Mode.SRC_IN);
                                    binding.scoreTextView.setTextColor(mDownvotedColor);
                                    binding.topScoreTextView.setTextColor(mDownvotedColor);
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    binding.downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                    binding.scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                    binding.topScoreTextView.setTextColor(mSecondaryTextColor);
                                }
                            }

                            if (currentPosition == position) {
                                binding.upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                                    binding.scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                            comment.getScore() + comment.getVoteType()));
                                    binding.topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                            Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                                    comment.getScore() + comment.getVoteType())));
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
                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    int position = getBindingAdapterPosition();
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

            binding.authorTextView.setOnClickListener(view -> {
                Comment comment = getCurrentComment(this);
                if (comment == null || comment.isAuthorDeleted()) {
                    return;
                }
                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, comment.getAuthor());
                mActivity.startActivity(intent);
            });

            binding.authorIconImageView.setOnClickListener(view -> {
                binding.authorTextView.performClick();
            });

            binding.expandButton.setOnClickListener(view -> {
                if (binding.expandButton.getVisibility() == View.VISIBLE) {
                    int commentPosition = mIsSingleCommentThreadMode ? getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                    Comment comment = getCurrentComment(this);
                    if (comment != null) {
                        if (mVisibleComments.get(commentPosition).isExpanded()) {
                            collapseChildren(commentPosition);
                            if (comment.getChildCount() > 0) {
                                binding.expandButton.setText("+" + comment.getChildCount());
                            }
                            binding.expandButton.setCompoundDrawablesWithIntrinsicBounds(expandDrawable, null, null, null);
                        } else {
                            comment.setExpanded(true);
                            ArrayList<Comment> newList = new ArrayList<>();
                            expandChildren(mVisibleComments.get(commentPosition).getChildren(), newList);
                            mVisibleComments.get(commentPosition).setExpanded(true);
                            mVisibleComments.addAll(commentPosition + 1, newList);

                            if (mIsSingleCommentThreadMode) {
                                notifyItemRangeInserted(commentPosition + 2, newList.size());
                            } else {
                                notifyItemRangeInserted(commentPosition + 1, newList.size());
                            }
                            if (mAlwaysShowChildCommentCount && comment.getChildCount() > 0) {
                                binding.expandButton.setText("+" + comment.getChildCount());
                            } else {
                                binding.expandButton.setText("");
                            }
                            binding.expandButton.setCompoundDrawablesWithIntrinsicBounds(collapseDrawable, null, null, null);
                        }
                    }
                } else if (mFullyCollapseComment) {
                    int commentPosition = mIsSingleCommentThreadMode ? getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                    if (commentPosition >= 0 && commentPosition < mVisibleComments.size()) {
                        collapseChildren(commentPosition);
                    }
                }
            });

            if (mSwapTapAndLong) {
                if (mCommentToolbarHideOnClick) {
                    View.OnLongClickListener hideToolbarOnLongClickListener = view -> hideToolbar();
                    itemView.setOnLongClickListener(hideToolbarOnLongClickListener);
                    binding.commentTimeTextView.setOnLongClickListener(hideToolbarOnLongClickListener);
                    mMarkwonAdapter.setOnLongClickListener(v -> {
                        if (v instanceof TextView) {
                            if (((TextView) v).getSelectionStart() == -1 && ((TextView) v).getSelectionEnd() == -1) {
                                hideToolbar();
                            }
                        }
                        return true;
                    });
                }
                mMarkwonAdapter.setOnClickListener(v -> {
                    if (v instanceof SpoilerOnClickTextView) {
                        if (((SpoilerOnClickTextView) v).isSpoilerOnClick()) {
                            ((SpoilerOnClickTextView) v).setSpoilerOnClick(false);
                            return;
                        }
                    }
                    expandComments();
                });
                itemView.setOnClickListener(view -> expandComments());
            } else {
                if (mCommentToolbarHideOnClick) {
                    mMarkwonAdapter.setOnClickListener(view -> {
                        if (view instanceof SpoilerOnClickTextView) {
                            if (((SpoilerOnClickTextView) view).isSpoilerOnClick()) {
                                ((SpoilerOnClickTextView) view).setSpoilerOnClick(false);
                                return;
                            }
                        }
                        hideToolbar();
                    });
                    View.OnClickListener hideToolbarOnClickListener = view -> hideToolbar();
                    itemView.setOnClickListener(hideToolbarOnClickListener);
                    binding.commentTimeTextView.setOnClickListener(hideToolbarOnClickListener);
                }
                mMarkwonAdapter.setOnLongClickListener(view -> {
                    if (view instanceof TextView) {
                        if (((TextView) view).getSelectionStart() == -1 && ((TextView) view).getSelectionEnd() == -1) {
                            expandComments();
                        }
                    }
                    return true;
                });
                itemView.setOnLongClickListener(view -> {
                    expandComments();
                    return true;
                });
            }
        }

        private boolean expandComments() {
            binding.expandButton.performClick();
            return true;
        }

        private boolean hideToolbar() {
            if (binding.bottomConstraintLayout.getLayoutParams().height == 0) {
                binding.bottomConstraintLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                binding.topScoreTextView.setVisibility(View.GONE);
                mFragment.delayTransition();
            } else {
                mFragment.delayTransition();
                binding.bottomConstraintLayout.getLayoutParams().height = 0;
                if (!mHideTheNumberOfVotes) {
                    binding.topScoreTextView.setVisibility(View.VISIBLE);
                }
            }
            return true;
        }
    }

    @Nullable
    private Comment getCurrentComment(RecyclerView.ViewHolder holder) {
        return getCurrentComment(holder.getBindingAdapterPosition());
    }

    @Nullable
    private Comment getCurrentComment(int position) {
        if (mIsSingleCommentThreadMode) {
            if (position - 1 >= 0 && position - 1 < mVisibleComments.size()) {
                return mVisibleComments.get(position - 1);
            }
        } else {
            if (position >= 0 && position < mVisibleComments.size()) {
                return mVisibleComments.get(position);
            }
        }

        return null;
    }

    class CommentFullyCollapsedViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentFullyCollapsedBinding binding;

        public CommentFullyCollapsedViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCommentFullyCollapsedBinding.bind(itemView);

            if (mActivity.typeface != null) {
                binding.userNameTextView.setTypeface(mActivity.typeface);
                binding.childCountTextView.setTypeface(mActivity.typeface);
                binding.scoreTextView.setTypeface(mActivity.typeface);
                binding.timeTextView.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mFullyCollapsedCommentBackgroundColor);
            binding.userNameTextView.setTextColor(mUsernameColor);
            binding.childCountTextView.setTextColor(mSecondaryTextColor);
            binding.scoreTextView.setTextColor(mSecondaryTextColor);
            binding.timeTextView.setTextColor(mSecondaryTextColor);

            if (mShowCommentDivider) {
                if (mDividerType == DIVIDER_NORMAL) {
                    binding.commentDivider.setBackgroundColor(mDividerColor);
                    binding.commentDivider.setVisibility(View.VISIBLE);
                }
            }

            if (mShowAuthorAvatar) {
                binding.authorIconImageView.setVisibility(View.VISIBLE);
            } else {
                binding.userNameTextView.setPaddingRelative(0, binding.userNameTextView.getPaddingTop(), binding.userNameTextView.getPaddingEnd(), binding.userNameTextView.getPaddingBottom());
            }

            itemView.setOnClickListener(view -> {
                int commentPosition = mIsSingleCommentThreadMode ? getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                if (commentPosition >= 0 && commentPosition < mVisibleComments.size()) {
                    Comment comment = getCurrentComment(this);
                    if (comment != null) {
                        comment.setExpanded(true);
                        ArrayList<Comment> newList = new ArrayList<>();
                        expandChildren(mVisibleComments.get(commentPosition).getChildren(), newList);
                        mVisibleComments.get(commentPosition).setExpanded(true);
                        mVisibleComments.addAll(commentPosition + 1, newList);

                        if (mIsSingleCommentThreadMode) {
                            notifyItemChanged(commentPosition + 1);
                            notifyItemRangeInserted(commentPosition + 2, newList.size());
                        } else {
                            notifyItemChanged(commentPosition);
                            notifyItemRangeInserted(commentPosition + 1, newList.size());
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(view -> {
                itemView.performClick();
                return true;
            });
        }
    }

    class LoadMoreChildCommentsViewHolder extends RecyclerView.ViewHolder {
        private final ItemLoadMoreCommentsPlaceholderBinding binding;

        LoadMoreChildCommentsViewHolder(View itemView) {
            super(itemView);
            binding = ItemLoadMoreCommentsPlaceholderBinding.bind(itemView);
            if (mShowCommentDivider) {
                if (mDividerType == DIVIDER_NORMAL) {
                    binding.dividerPlaceholder.setBackgroundColor(mDividerColor);
                    binding.dividerPlaceholder.setVisibility(View.VISIBLE);
                }
            }

            if (mActivity.typeface != null) {
                binding.placeholderTextView.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mCommentBackgroundColor);
            binding.placeholderTextView.setTextColor(mPrimaryTextColor);
        }
    }

    class LoadCommentsViewHolder extends RecyclerView.ViewHolder {
        LoadCommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemLoadCommentsBinding binding = ItemLoadCommentsBinding.bind(itemView);
            binding.commentProgressBar.setBackgroundTintList(ColorStateList.valueOf(mCircularProgressBarBackgroundColor));
            binding.commentProgressBar.setColorSchemeColors(mColorAccent);
        }
    }

    class LoadCommentsFailedViewHolder extends RecyclerView.ViewHolder {
        LoadCommentsFailedViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemLoadCommentsFailedPlaceholderBinding binding =
                    ItemLoadCommentsFailedPlaceholderBinding.bind(itemView);
            itemView.setOnClickListener(view -> mCommentRecyclerViewAdapterCallback.retryFetchingComments());
            if (mActivity.typeface != null) {
                binding.errorTextView.setTypeface(mActivity.typeface);
            }
            binding.errorTextView.setTextColor(mSecondaryTextColor);
        }
    }

    class NoCommentViewHolder extends RecyclerView.ViewHolder {
        NoCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemNoCommentPlaceholderBinding binding = ItemNoCommentPlaceholderBinding.bind(itemView);
            if (mActivity.typeface != null) {
                binding.errorTextView.setTypeface(mActivity.typeface);
            }
            binding.errorTextView.setTextColor(mSecondaryTextColor);
        }
    }

    class IsLoadingMoreCommentsViewHolder extends RecyclerView.ViewHolder {
        IsLoadingMoreCommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemCommentFooterLoadingBinding binding = ItemCommentFooterLoadingBinding.bind(itemView);
            binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
        }
    }

    class LoadMoreCommentsFailedViewHolder extends RecyclerView.ViewHolder {
        LoadMoreCommentsFailedViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemCommentFooterErrorBinding binding = ItemCommentFooterErrorBinding.bind(itemView);
            if (mActivity.typeface != null) {
                binding.errorTextView.setTypeface(mActivity.typeface);
                binding.retryButton.setTypeface(mActivity.typeface);
            }
            binding.errorTextView.setText(R.string.load_comments_failed);
            binding.retryButton.setOnClickListener(view -> mCommentRecyclerViewAdapterCallback.retryFetchingMoreComments());
            binding.errorTextView.setTextColor(mSecondaryTextColor);
            binding.retryButton.setBackgroundTintList(ColorStateList.valueOf(mColorPrimaryLightTheme));
            binding.retryButton.setTextColor(mButtonTextColor);
        }
    }

    class ViewAllCommentsViewHolder extends RecyclerView.ViewHolder {

        ViewAllCommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(view -> {
                if (mActivity != null && mActivity instanceof ViewPostDetailActivity) {
                    mIsSingleCommentThreadMode = false;
                    mSingleCommentId = null;
                    notifyItemRemoved(0);
                    mFragment.changeToNormalThreadMode();
                }
            });

            if (mActivity.typeface != null) {
                ((TextView) itemView).setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundTintList(ColorStateList.valueOf(mCommentBackgroundColor));
            ((TextView) itemView).setTextColor(mColorAccent);
        }
    }
}
