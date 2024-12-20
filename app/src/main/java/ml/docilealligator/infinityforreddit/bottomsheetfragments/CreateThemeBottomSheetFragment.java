package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentCreateThemeBottomSheetBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateThemeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    private BaseActivity activity;

    public interface SelectBaseThemeBottomSheetFragmentListener {
        void importTheme();
    }

    public CreateThemeBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentCreateThemeBottomSheetBinding binding =
                FragmentCreateThemeBottomSheetBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        binding.importThemeTextView.setOnClickListener(view -> {
            ((SelectBaseThemeBottomSheetFragmentListener) activity).importTheme();
            dismiss();
        });

        binding.lightThemeTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CustomizeThemeActivity.class);
            intent.putExtra(CustomizeThemeActivity.EXTRA_CREATE_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_IS_PREDEFIINED_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, getString(R.string.theme_name_indigo));
            startActivity(intent);
            dismiss();
        });

        binding.darkThemeTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CustomizeThemeActivity.class);
            intent.putExtra(CustomizeThemeActivity.EXTRA_CREATE_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_IS_PREDEFIINED_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, getString(R.string.theme_name_indigo_dark));
            startActivity(intent);
            dismiss();
        });

        binding.amoledThemeTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CustomizeThemeActivity.class);
            intent.putExtra(CustomizeThemeActivity.EXTRA_CREATE_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_IS_PREDEFIINED_THEME, true);
            intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, getString(R.string.theme_name_indigo_amoled));
            startActivity(intent);
            dismiss();
        });
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}
