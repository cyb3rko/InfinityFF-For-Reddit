package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.CustomThemePreviewActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.databinding.FragmentThemePreviewPostsBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemePreviewPostsFragment extends Fragment {
    private FragmentThemePreviewPostsBinding binding;
    private CustomThemePreviewActivity activity;

    public ThemePreviewPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentThemePreviewPostsBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        CustomTheme customTheme = activity.getCustomTheme();

        binding.cardView.setBackgroundTintList(ColorStateList.valueOf(customTheme.cardViewBackgroundColor));
        Glide.with(this).load(R.drawable.subreddit_default_icon)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                .into(binding.iconGifImageView);
        binding.subredditNameTextView.setTextColor(customTheme.subreddit);
        binding.userTextView.setTextColor(customTheme.username);
        binding.postTimeTextViewBest.setTextColor(customTheme.secondaryTextColor);
        binding.titleTextViewBest.setTextColor(customTheme.postTitleColor);
        binding.contentTextView.setTextColor(customTheme.postContentColor);
        binding.stickiedPostImageView.setColorFilter(customTheme.stickiedPostIconTint, PorterDuff.Mode.SRC_IN);
        binding.typeTextView.setBackgroundColor(customTheme.postTypeBackgroundColor);
        binding.typeTextView.setBorderColor(customTheme.postTypeBackgroundColor);
        binding.typeTextView.setTextColor(customTheme.postTypeTextColor);
        binding.spoilerCustomTextView.setBackgroundColor(customTheme.spoilerBackgroundColor);
        binding.spoilerCustomTextView.setBorderColor(customTheme.spoilerBackgroundColor);
        binding.spoilerCustomTextView.setTextColor(customTheme.spoilerTextColor);
        binding.nsfwTextView.setBackgroundColor(customTheme.nsfwBackgroundColor);
        binding.nsfwTextView.setBorderColor(customTheme.nsfwBackgroundColor);
        binding.nsfwTextView.setTextColor(customTheme.nsfwTextColor);
        binding.flairCustomTextView.setBackgroundColor(customTheme.flairBackgroundColor);
        binding.flairCustomTextView.setBorderColor(customTheme.flairBackgroundColor);
        binding.flairCustomTextView.setTextColor(customTheme.flairTextColor);
        binding.awardsTextView.setBackgroundColor(customTheme.awardsBackgroundColor);
        binding.awardsTextView.setBorderColor(customTheme.awardsBackgroundColor);
        binding.awardsTextView.setTextColor(customTheme.awardsTextColor);
        binding.archivedImageView.setColorFilter(customTheme.archivedTint, PorterDuff.Mode.SRC_IN);
        binding.lockedImageView.setColorFilter(customTheme.lockedIconTint, PorterDuff.Mode.SRC_IN);
        binding.crosspostImageView.setColorFilter(customTheme.crosspostIconTint, PorterDuff.Mode.SRC_IN);
        binding.linkTextView.setTextColor(customTheme.secondaryTextColor);
        binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(customTheme.colorAccent));
        binding.imageViewNoPreviewLink.setBackgroundColor(customTheme.noPreviewPostTypeBackgroundColor);
        binding.plusButton.setColorFilter(customTheme.postIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.scoreTextView.setTextColor(customTheme.postIconAndInfoColor);
        binding.minusButton.setColorFilter(customTheme.postIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.commentsCount.setTextColor(customTheme.postIconAndInfoColor);
        Drawable commentIcon = AppCompatResources.getDrawable(activity, R.drawable.ic_comment_grey_24dp);
        if (commentIcon != null) {
            commentIcon.setTint(customTheme.postIconAndInfoColor);
        }
        binding.commentsCount.setCompoundDrawablesWithIntrinsicBounds(commentIcon, null, null, null);
        binding.saveButton.setColorFilter(customTheme.postIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.shareButton.setColorFilter(customTheme.postIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

        if (activity.typeface != null) {
            binding.subredditNameTextView.setTypeface(activity.typeface);
            binding.userTextView.setTypeface(activity.typeface);
            binding.postTimeTextViewBest.setTypeface(activity.typeface);
            binding.typeTextView.setTypeface(activity.typeface);
            binding.spoilerCustomTextView.setTypeface(activity.typeface);
            binding.nsfwTextView.setTypeface(activity.typeface);
            binding.flairCustomTextView.setTypeface(activity.typeface);
            binding.awardsTextView.setTypeface(activity.typeface);
            binding.linkTextView.setTypeface(activity.typeface);
            binding.scoreTextView.setTypeface(activity.typeface);
            binding.commentsCount.setTypeface(activity.typeface);
        }
        if (activity.titleTypeface != null) {
            binding.titleTextViewBest.setTypeface(activity.titleTypeface);
        }
        if (activity.contentTypeface != null) {
            binding.contentTextView.setTypeface(activity.contentTypeface);
        }
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CustomThemePreviewActivity) context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
