package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentShareLinkBottomSheetBinding;
import ml.docilealligator.infinityforreddit.post.Post;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareLinkBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_POST_LINK = "EPL";
    public static final String EXTRA_MEDIA_LINK = "EML";
    public static final String EXTRA_MEDIA_TYPE = "EMT";

    private BaseActivity activity;

    public ShareLinkBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentShareLinkBottomSheetBinding binding =
                FragmentShareLinkBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        String postLink = getArguments().getString(EXTRA_POST_LINK);
        String mediaLink = getArguments().containsKey(EXTRA_MEDIA_LINK) ? getArguments().getString(EXTRA_MEDIA_LINK) : null;

        binding.postLinkTextView.setText(postLink);

        if (mediaLink != null) {
            binding.mediaLinkTextView.setVisibility(View.VISIBLE);
            binding.shareMediaLinkTextView.setVisibility(View.VISIBLE);
            binding.copyMediaLinkTextView.setVisibility(View.VISIBLE);

            binding.mediaLinkTextView.setText(mediaLink);

            int mediaType = getArguments().getInt(EXTRA_MEDIA_TYPE);
            switch (mediaType) {
                case Post.IMAGE_TYPE:
                    binding.shareMediaLinkTextView.setText(R.string.share_image_link);
                    binding.copyMediaLinkTextView.setText(R.string.copy_image_link);
                    binding.shareMediaLinkTextView.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_image_24dp), null, null, null);
                    break;
                case Post.GIF_TYPE:
                    binding.shareMediaLinkTextView.setText(R.string.share_gif_link);
                    binding.copyMediaLinkTextView.setText(R.string.copy_gif_link);
                    binding.shareMediaLinkTextView.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_image_24dp), null, null, null);
                    break;
                case Post.VIDEO_TYPE:
                    binding.shareMediaLinkTextView.setText(R.string.share_video_link);
                    binding.copyMediaLinkTextView.setText(R.string.copy_video_link);
                    binding.shareMediaLinkTextView.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_outline_video_24dp), null, null, null);
                    break;
                case Post.LINK_TYPE:
                case Post.NO_PREVIEW_LINK_TYPE:
                    binding.shareMediaLinkTextView.setText(R.string.share_link);
                    binding.copyMediaLinkTextView.setText(R.string.copy_link);
                    break;
            }

            binding.shareMediaLinkTextView.setOnClickListener(view -> {
                shareLink(mediaLink);
                dismiss();
            });
            binding.copyMediaLinkTextView.setOnClickListener(view -> {
                copyLink(mediaLink);
                dismiss();
            });
        }

        binding.sharePostLinkTextView.setOnClickListener(view -> {
            shareLink(postLink);
            dismiss();
        });
        binding.copyPostLinkTextView.setOnClickListener(view -> {
            copyLink(postLink);
            dismiss();
        });
        return rootView;
    }

    private void shareLink(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, link);
            activity.startActivity(Intent.createChooser(intent, getString(R.string.share)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
        }
    }

    private void copyLink(String link) {
        activity.copyLink(link);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}
