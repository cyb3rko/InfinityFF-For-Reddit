package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.activities.SelectedSubredditsAndUsersActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentSelectSubredditsOrUsersOptionsBottomSheetBinding;

public class SelectSubredditsOrUsersOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    private SelectedSubredditsAndUsersActivity activity;

    public SelectSubredditsOrUsersOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSelectSubredditsOrUsersOptionsBottomSheetBinding binding =
                FragmentSelectSubredditsOrUsersOptionsBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        binding.selectSubredditsTextView.setOnClickListener(view -> {
            activity.selectSubreddits();
            dismiss();
        });

        binding.selectUsersTextView.setOnClickListener(view -> {
            activity.selectUsers();
            dismiss();
        });
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SelectedSubredditsAndUsersActivity) context;
    }
}