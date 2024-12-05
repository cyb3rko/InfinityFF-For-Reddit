package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentFilteredThingFabMoreOptionsBottomSheetBinding;

public class FilteredThingFABMoreOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final int FAB_OPTION_FILTER = 0;
    public static final int FAB_OPTION_HIDE_READ_POSTS = 1;

    private FragmentFilteredThingFabMoreOptionsBottomSheetBinding binding;
    private FABOptionSelectionCallback activity;

    public FilteredThingFABMoreOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilteredThingFabMoreOptionsBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        binding.filterTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_FILTER);
            dismiss();
        });

        binding.hideReadPostsTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_HIDE_READ_POSTS);
            dismiss();
        });
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (FABOptionSelectionCallback) context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface FABOptionSelectionCallback {
        void fabOptionSelected(int option);
    }
}