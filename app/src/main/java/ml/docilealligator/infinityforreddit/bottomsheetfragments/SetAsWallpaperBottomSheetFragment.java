package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.SetAsWallpaperCallback;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentSetAsWallpaperBottomSheetBinding;

public class SetAsWallpaperBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_VIEW_PAGER_POSITION = "EVPP";

    private Activity mActivity;

    public SetAsWallpaperBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSetAsWallpaperBottomSheetBinding binding =
                FragmentSetAsWallpaperBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        Bundle bundle = getArguments();
        int viewPagerPosition = bundle == null ? -1 : bundle.getInt(EXTRA_VIEW_PAGER_POSITION);

        binding.bothTextView.setOnClickListener(view -> {
            if (mActivity instanceof SetAsWallpaperCallback) {
                ((SetAsWallpaperCallback) mActivity).setToBoth(viewPagerPosition);
            }
            dismiss();
        });

        binding.homeScreenTextView.setVisibility(View.VISIBLE);
        binding.lockScreenTextView.setVisibility(View.VISIBLE);

        binding.homeScreenTextView.setOnClickListener(view -> {
            if (mActivity instanceof SetAsWallpaperCallback) {
                ((SetAsWallpaperCallback) mActivity).setToHomeScreen(viewPagerPosition);
            }
            dismiss();
        });

        binding.lockScreenTextView.setOnClickListener(view -> {
            if (mActivity instanceof SetAsWallpaperCallback) {
                ((SetAsWallpaperCallback) mActivity).setToLockScreen(viewPagerPosition);
            }
            dismiss();
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }
}