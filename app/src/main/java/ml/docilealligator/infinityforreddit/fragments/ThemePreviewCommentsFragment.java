package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.activities.CustomThemePreviewActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.databinding.FragmentThemePreviewCommentsBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemePreviewCommentsFragment extends Fragment {
    private FragmentThemePreviewCommentsBinding binding;
    private CustomThemePreviewActivity activity;

    public ThemePreviewCommentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentThemePreviewCommentsBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        CustomTheme customTheme = activity.getCustomTheme();
        binding.linearLayout.setBackgroundColor(customTheme.commentBackgroundColor);
        binding.authorTypeImageView.setColorFilter(customTheme.moderator, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.authorTextView.setTextColor(customTheme.moderator);
        binding.commentTimeTextView.setTextColor(customTheme.secondaryTextColor);
        binding.commentMarkdownView.setTextColor(customTheme.commentColor);
        binding.authorFlairTextView.setTextColor(customTheme.authorFlairTextColor);
        binding.divider.setBackgroundColor(customTheme.dividerColor);
        binding.upvoteButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.scoreTextView.setTextColor(customTheme.commentIconAndInfoColor);
        binding.downvoteButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.moreButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.expandButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.saveButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.replyButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

        binding.linearLayoutAwardBackground.setBackgroundColor(customTheme.awardedCommentBackgroundColor);
        binding.authorTypeImageViewAwardBackground.setColorFilter(customTheme.moderator, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.authorTextViewAwardBackground.setTextColor(customTheme.moderator);
        binding.commentTimeTextViewAwardBackground.setTextColor(customTheme.secondaryTextColor);
        binding.commentMarkdownViewAwardBackground.setTextColor(customTheme.commentColor);
        binding.authorFlairTextViewAwardBackground.setTextColor(customTheme.authorFlairTextColor);
        binding.dividerAwardBackground.setBackgroundColor(customTheme.dividerColor);
        binding.upvoteButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.scoreTextViewAwardBackground.setTextColor(customTheme.commentIconAndInfoColor);
        binding.downvoteButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.moreButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.expandButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.saveButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.replyButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

        binding.linearLayoutFullyCollapsed.setBackgroundColor(customTheme.fullyCollapsedCommentBackgroundColor);
        binding.authorTextViewFullyCollapsed.setTextColor(customTheme.username);
        binding.scoreTextViewFullyCollapsed.setTextColor(customTheme.secondaryTextColor);
        binding.timeTextViewFullyCollapsed.setTextColor(customTheme.secondaryTextColor);
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
