package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivitySuicidePreventionBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class SuicidePreventionActivity extends BaseActivity {
    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_RETURN_QUERY = "ERQ";

    private ActivitySuicidePreventionBinding binding;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        binding = ActivitySuicidePreventionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        binding.linearLayoutCheckBoxWrapper.setOnClickListener(view -> {
            binding.doNotShowThisAgainCheckBox.performClick();
        });

        binding.continueButton.setOnClickListener(view -> {
            if (binding.doNotShowThisAgainCheckBox.isChecked()) {
                mSharedPreferences.edit().putBoolean(SharedPreferencesUtils.SHOW_SUICIDE_PREVENTION_ACTIVITY, false).apply();
            }
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_RETURN_QUERY, getIntent().getStringExtra(EXTRA_QUERY));
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.linearLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        binding.quoteTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        binding.doNotShowThisAgainTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        binding.continueButton.setBackgroundTintList(ColorStateList.valueOf(mCustomThemeWrapper.getColorPrimaryLightTheme()));
        binding.continueButton.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        if (typeface != null) {
            binding.quoteTextView.setTypeface(typeface);
            binding.doNotShowThisAgainTextView.setTypeface(typeface);
            binding.continueButton.setTypeface(typeface);
        }
    }
}