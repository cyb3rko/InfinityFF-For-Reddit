package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.TranslationFragmentRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.FragmentTranslationBinding;

public class TranslationFragment extends Fragment {
    @Inject
    CustomThemeWrapper customThemeWrapper;
    private BaseActivity activity;

    public TranslationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentTranslationBinding binding = FragmentTranslationBinding.inflate(getLayoutInflater(), container, false);
        View rootView = binding.getRoot();

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        TranslationFragmentRecyclerViewAdapter adapter = new TranslationFragmentRecyclerViewAdapter(activity, customThemeWrapper);
        binding.recyclerViewTranslationFragment.setAdapter(adapter);

        rootView.setBackgroundColor(customThemeWrapper.getBackgroundColor());

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}