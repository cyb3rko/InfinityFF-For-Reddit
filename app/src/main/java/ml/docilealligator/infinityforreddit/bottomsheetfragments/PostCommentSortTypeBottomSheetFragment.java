package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostCommentSortTypeBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostCommentSortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_CURRENT_SORT_TYPE = "ECST";

    private BaseActivity activity;

    public PostCommentSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    public static PostCommentSortTypeBottomSheetFragment getNewInstance(SortType.Type currentSortType) {
        PostCommentSortTypeBottomSheetFragment fragment = new PostCommentSortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_CURRENT_SORT_TYPE, currentSortType);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentPostCommentSortTypeBottomSheetBinding binding =
                FragmentPostCommentSortTypeBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        SortType.Type currentSortType = (SortType.Type) getArguments().getSerializable(EXTRA_CURRENT_SORT_TYPE);
        if (currentSortType.equals(SortType.Type.BEST) || currentSortType.equals(SortType.Type.CONFIDENCE)) {
            binding.bestTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.bestTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.TOP)) {
            binding.topTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.topTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.NEW)) {
            binding.newTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.newTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.CONTROVERSIAL)) {
            binding.controversialTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.controversialTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.OLD)) {
            binding.oldTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.oldTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.RANDOM)) {
            binding.randomTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.randomTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.QA)) {
            binding.qaTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.qaTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.LIVE)) {
            binding.liveTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.liveTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        }

        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        binding.bestTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.CONFIDENCE));
            dismiss();
        });

        binding.topTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.TOP));
            dismiss();
        });

        binding.newTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.NEW));
            dismiss();
        });

        binding.controversialTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.CONTROVERSIAL));
            dismiss();
        });

        binding.oldTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.OLD));
            dismiss();
        });

        binding.randomTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.RANDOM));
            dismiss();
        });

        binding.qaTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.QA));
            dismiss();
        });

        binding.liveTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.LIVE));
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
        this.activity = (BaseActivity) context;
    }
}
