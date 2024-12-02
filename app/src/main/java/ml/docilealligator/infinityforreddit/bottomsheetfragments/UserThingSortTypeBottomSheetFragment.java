package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
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
import ml.docilealligator.infinityforreddit.databinding.FragmentUserThingSortTypeBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserThingSortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_CURRENT_SORT_TYPE = "ECST";

    private BaseActivity activity;

    public UserThingSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    public static UserThingSortTypeBottomSheetFragment getNewInstance(SortType currentSortType) {
        UserThingSortTypeBottomSheetFragment fragment = new UserThingSortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CURRENT_SORT_TYPE, currentSortType.getType().fullName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentUserThingSortTypeBottomSheetBinding binding =
                FragmentUserThingSortTypeBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        String currentSortType = getArguments().getString(EXTRA_CURRENT_SORT_TYPE);
        if (currentSortType.equals(SortType.Type.NEW.fullName)) {
            binding.newTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.newTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.HOT.fullName)) {
            binding.hotTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.hotTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.TOP.fullName)) {
            binding.topTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.topTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.CONTROVERSIAL.fullName)) {
            binding.controversialTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.controversialTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        binding.newTypeTextView.setOnClickListener(view -> {
            if (activity != null) {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.NEW));
            }
            dismiss();
        });

        binding.hotTypeTextView.setOnClickListener(view -> {
            if (activity != null) {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.HOT));
            }
            dismiss();
        });

        binding.topTypeTextView.setOnClickListener(view -> {
            if (activity != null) {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.TOP.name());
            }
            dismiss();
        });

        binding.controversialTypeTextView.setOnClickListener(view -> {
            if (activity != null) {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.CONTROVERSIAL.name());
            }
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
