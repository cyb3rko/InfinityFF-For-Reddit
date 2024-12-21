package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostLayotBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostLayoutBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    private BaseActivity activity;

    public PostLayoutBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentPostLayotBottomSheetBinding binding =
                FragmentPostLayotBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        binding.cardLayoutTextView.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_CARD);
            dismiss();
        });
        binding.compactLayoutTextView.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_COMPACT);
            dismiss();
        });
        binding.galleryLayoutTextView.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_GALLERY);
            dismiss();
        });
        binding.cardLayout2TextView.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_CARD_2);
            dismiss();
        });
        binding.cardLayout3TextView.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_CARD_3);
            dismiss();
        });
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (BaseActivity) context;
    }

    public interface PostLayoutSelectionCallback {
        void postLayoutSelected(int postLayout);
    }
}
