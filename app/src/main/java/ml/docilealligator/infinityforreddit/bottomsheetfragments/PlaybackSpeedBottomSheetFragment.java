package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPlaybackSpeedBinding;
import ml.docilealligator.infinityforreddit.fragments.ViewImgurVideoFragment;
import ml.docilealligator.infinityforreddit.fragments.ViewRedditGalleryVideoFragment;

public class PlaybackSpeedBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_PLAYBACK_SPEED = "EPS";
    private Activity activity;

    public PlaybackSpeedBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentPlaybackSpeedBinding binding =
                FragmentPlaybackSpeedBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        int playbackSpeed = getArguments().getInt(EXTRA_PLAYBACK_SPEED, ViewVideoActivity.PLAYBACK_SPEED_NORMAL);
        switch (playbackSpeed) {
            case ViewVideoActivity.PLAYBACK_SPEED_25:
                binding.playbackSpeed025TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_50:
                binding.playbackSpeed050TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_75:
                binding.playbackSpeed075TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_NORMAL:
                binding.playbackSpeedNormalTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_125:
                binding.playbackSpeed125TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_150:
                binding.playbackSpeed150TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_175:
                binding.playbackSpeed175TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_200:
                binding.playbackSpeed200TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
        }

        binding.playbackSpeed025TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_25);
            dismiss();
        });

        binding.playbackSpeed050TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_50);
            dismiss();
        });

        binding.playbackSpeed075TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_75);
            dismiss();
        });

        binding.playbackSpeedNormalTextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_NORMAL);
            dismiss();
        });

        binding.playbackSpeed125TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_125);
            dismiss();
        });

        binding.playbackSpeed150TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_150);
            dismiss();
        });

        binding.playbackSpeed175TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_175);
            dismiss();
        });

        binding.playbackSpeed200TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_200);
            dismiss();
        });
        return rootView;
    }

    private void setPlaybackSpeed(int playbackSpeed) {
        if (activity instanceof ViewVideoActivity) {
            ((ViewVideoActivity) activity).setPlaybackSpeed(playbackSpeed);
        } else {
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof ViewImgurVideoFragment) {
                ((ViewImgurVideoFragment) parentFragment).setPlaybackSpeed(playbackSpeed);
            } else if (parentFragment instanceof ViewRedditGalleryVideoFragment) {
                ((ViewRedditGalleryVideoFragment) parentFragment).setPlaybackSpeed(playbackSpeed);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}