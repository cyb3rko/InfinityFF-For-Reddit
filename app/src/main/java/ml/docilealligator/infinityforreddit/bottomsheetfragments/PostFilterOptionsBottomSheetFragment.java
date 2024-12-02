package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.activities.PostFilterPreferenceActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostFilterOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class PostFilterOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_POST_FILTER = "EPF";

    private PostFilterPreferenceActivity activity;

    public PostFilterOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentPostFilterOptionsBottomSheetBinding binding =
                FragmentPostFilterOptionsBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        PostFilter postFilter = getArguments().getParcelable(EXTRA_POST_FILTER);

        binding.editTextView.setOnClickListener(view -> {
            activity.editPostFilter(postFilter);
            dismiss();
        });

        binding.applyToTextView.setOnClickListener(view -> {
            activity.applyPostFilterTo(postFilter);
            dismiss();
        });

        binding.deleteTextView.setOnClickListener(view -> {
            activity.deletePostFilter(postFilter);
            dismiss();
        });

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, activity.typeface);
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (PostFilterPreferenceActivity) context;
    }
}