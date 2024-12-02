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
import ml.docilealligator.infinityforreddit.databinding.FragmentSearchPostSortTypeBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchPostSortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_CURRENT_SORT_TYPE = "ECST";

    private BaseActivity activity;

    public SearchPostSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    public static SearchPostSortTypeBottomSheetFragment getNewInstance(SortType currentSortType) {
        SearchPostSortTypeBottomSheetFragment fragment = new SearchPostSortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CURRENT_SORT_TYPE, currentSortType.getType().fullName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSearchPostSortTypeBottomSheetBinding binding =
                FragmentSearchPostSortTypeBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        String currentSortType = getArguments().getString(EXTRA_CURRENT_SORT_TYPE);
        if (currentSortType.equals(SortType.Type.RELEVANCE.fullName)) {
            binding.relevanceTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.relevanceTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.HOT.fullName)) {
            binding.hotTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.hotTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.TOP.fullName)) {
            binding.topTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.topTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.NEW.fullName)) {
            binding.newTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.newTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.RISING.fullName)) {
            binding.commentsTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.commentsTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        }

        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        binding.relevanceTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.RELEVANCE.name());
            dismiss();
        });

        binding.hotTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.HOT.name());
            dismiss();
        });

        binding.topTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.TOP.name());
            dismiss();
        });

        binding.newTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.NEW));
            dismiss();
        });

        binding.commentsTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.COMMENTS.name());
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
