package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentSortTimeBottomSheetBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class SortTimeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_SORT_TYPE = "EST";

    private BaseActivity activity;

    public SortTimeBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSortTimeBottomSheetBinding binding =
                FragmentSortTimeBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        String sortType = getArguments() != null ? getArguments().getString(EXTRA_SORT_TYPE) : null;
        if (sortType == null) {
            dismiss();
            return rootView;
        }

        binding.hourTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.HOUR));
            dismiss();
        });

        binding.dayTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.DAY));
            dismiss();
        });

        binding.weekTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.WEEK));
            dismiss();
        });

        binding.monthTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.MONTH));
            dismiss();
        });

        binding.yearTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.YEAR));
            dismiss();
        });

        binding.allTimeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.ALL));
            dismiss();
        });
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (BaseActivity) context;
    }
}
