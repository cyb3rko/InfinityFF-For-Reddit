package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.databinding.FragmentCustomizeBottomAppBarBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomizeBottomAppBarFragment extends Fragment {
    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    private FragmentCustomizeBottomAppBarBinding binding;

    @Inject
    @Named("bottom_app_bar")
    SharedPreferences sharedPreferences;
    private SettingsActivity activity;
    private int mainActivityOptionCount;
    private int mainActivityOption1;
    private int mainActivityOption2;
    private int mainActivityOption3;
    private int mainActivityOption4;
    private int mainActivityFAB;
    private int otherActivitiesOptionCount;
    private int otherActivitiesOption1;
    private int otherActivitiesOption2;
    private int otherActivitiesOption3;
    private int otherActivitiesOption4;
    private int otherActivitiesFAB;

    public CustomizeBottomAppBarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCustomizeBottomAppBarBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        rootView.setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());

        applyCustomTheme();

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, activity.typeface);
        }

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        Resources resources = activity.getResources();
        String[] mainActivityOptions = resources.getStringArray(R.array.settings_main_activity_bottom_app_bar_options);
        String[] mainActivityOptionAnonymous = resources.getStringArray(R.array.settings_main_activity_bottom_app_bar_options_anonymous);
        String[] mainActivityOptionAnonymousValues = resources.getStringArray(R.array.settings_main_activity_bottom_app_bar_options_anonymous_values);
        String[] fabOptions;
        mainActivityOptionCount = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, 4);
        mainActivityOption1 = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, 0);
        mainActivityOption2 = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, 1);
        mainActivityOption3 = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, 2);
        mainActivityOption4 = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, 3);
        mainActivityFAB = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB, accountName == null ? 7: 0);

        binding.mainActivityOptionCountTextView.setText(Integer.toString(mainActivityOptionCount));
        binding.mainActivityOption1TextView.setText(mainActivityOptions[mainActivityOption1]);
        binding.mainActivityOption2TextView.setText(mainActivityOptions[mainActivityOption2]);
        binding.mainActivityOption3TextView.setText(mainActivityOptions[mainActivityOption3]);
        binding.mainActivityOption4TextView.setText(mainActivityOptions[mainActivityOption4]);

        if (accountName == null) {
            fabOptions = resources.getStringArray(R.array.settings_bottom_app_bar_fab_options_anonymous);
            ArrayList<String> mainActivityOptionAnonymousValuesList = new ArrayList<>(Arrays.asList(mainActivityOptionAnonymousValues));
            mainActivityOption1 = mainActivityOptionAnonymousValuesList.indexOf(Integer.toString(mainActivityOption1));
            mainActivityOption2 = mainActivityOptionAnonymousValuesList.indexOf(Integer.toString(mainActivityOption2));
            mainActivityOption3 = mainActivityOptionAnonymousValuesList.indexOf(Integer.toString(mainActivityOption3));
            mainActivityOption4 = mainActivityOptionAnonymousValuesList.indexOf(Integer.toString(mainActivityOption4));

            mainActivityFAB = mainActivityFAB >= 9 ? mainActivityFAB - 2 : mainActivityFAB - 1;
        } else {
            fabOptions = resources.getStringArray(R.array.settings_bottom_app_bar_fab_options);
        }

        binding.mainActivityFabTextView.setText(fabOptions[mainActivityFAB]);

        binding.mainActivityOptionCountLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_bottom_app_bar_option_count_options, mainActivityOptionCount / 2 - 1, (dialogInterface, i) -> {
                        mainActivityOptionCount = (i + 1) * 2;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, mainActivityOptionCount).apply();
                        binding.mainActivityOptionCountTextView.setText(Integer.toString(mainActivityOptionCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityOption1LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_1)
                    .setSingleChoiceItems(accountName == null ? mainActivityOptionAnonymous : mainActivityOptions, mainActivityOption1, (dialogInterface, i) -> {
                        mainActivityOption1 = i;
                        int optionToSaveToPreference = accountName == null ? Integer.parseInt(mainActivityOptionAnonymousValues[i]) : mainActivityOption1;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, optionToSaveToPreference).apply();
                        binding.mainActivityOption1TextView.setText(mainActivityOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityOption2LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_2)
                    .setSingleChoiceItems(accountName == null ? mainActivityOptionAnonymous : mainActivityOptions, mainActivityOption2, (dialogInterface, i) -> {
                        mainActivityOption2 = i;
                        int optionToSaveToPreference = accountName == null ? Integer.parseInt(mainActivityOptionAnonymousValues[i]) : mainActivityOption2;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, optionToSaveToPreference).apply();
                        binding.mainActivityOption2TextView.setText(mainActivityOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityOption3LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_3)
                    .setSingleChoiceItems(accountName == null ? mainActivityOptionAnonymous : mainActivityOptions, mainActivityOption3, (dialogInterface, i) -> {
                        mainActivityOption3 = i;
                        int optionToSaveToPreference = accountName == null ? Integer.parseInt(mainActivityOptionAnonymousValues[i]) : mainActivityOption3;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, optionToSaveToPreference).apply();
                        binding.mainActivityOption3TextView.setText(mainActivityOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityOption4LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_4)
                    .setSingleChoiceItems(accountName == null ? mainActivityOptionAnonymous : mainActivityOptions, mainActivityOption4, (dialogInterface, i) -> {
                        mainActivityOption4 = i;
                        int optionToSaveToPreference = accountName == null ? Integer.parseInt(mainActivityOptionAnonymousValues[i]) : mainActivityOption4;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, optionToSaveToPreference).apply();
                        binding.mainActivityOption4TextView.setText(mainActivityOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityFabLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_fab)
                    .setSingleChoiceItems(fabOptions, mainActivityFAB, (dialogInterface, i) -> {
                        mainActivityFAB = i;
                        int optionToSaveToPreference;
                        if (accountName == null) {
                            if (i >= 7) {
                                optionToSaveToPreference = i + 2;
                            } else {
                                optionToSaveToPreference = i + 1;
                            }
                        } else {
                            optionToSaveToPreference = i;
                        }
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB, optionToSaveToPreference).apply();
                        binding.mainActivityFabTextView.setText(fabOptions[mainActivityFAB]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        String[] otherActivitiesOptions = resources.getStringArray(R.array.settings_other_activities_bottom_app_bar_options);
        String[] otherActivitiesOptionAnonymous = resources.getStringArray(R.array.settings_other_activities_bottom_app_bar_options_anonymous);
        String[] otherActivitiesOptionAnonymousValues = resources.getStringArray(R.array.settings_other_activities_bottom_app_bar_options_anonymous_values);
        otherActivitiesOptionCount = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_COUNT, 4);
        otherActivitiesOption1 = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_1, 0);
        otherActivitiesOption2 = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_2, 1);
        otherActivitiesOption3 = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_3, 2);
        otherActivitiesOption4 = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_4, 3);
        otherActivitiesFAB = sharedPreferences.getInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, accountName == null ? 7: 0);

        binding.otherActivitiesOptionCountTextView.setText(Integer.toString(otherActivitiesOptionCount));
        binding.otherActivitiesOption1TextView.setText(otherActivitiesOptions[otherActivitiesOption1]);
        binding.otherActivitiesOption2TextView.setText(otherActivitiesOptions[otherActivitiesOption2]);
        binding.otherActivitiesOption3TextView.setText(otherActivitiesOptions[otherActivitiesOption3]);
        binding.otherActivitiesOption4TextView.setText(otherActivitiesOptions[otherActivitiesOption4]);

        if (accountName == null) {
            ArrayList<String> otherActivitiesOptionAnonymousValuesList = new ArrayList<>(Arrays.asList(otherActivitiesOptionAnonymousValues));
            otherActivitiesOption1 = otherActivitiesOptionAnonymousValuesList.indexOf(Integer.toString(otherActivitiesOption1));
            otherActivitiesOption2 = otherActivitiesOptionAnonymousValuesList.indexOf(Integer.toString(otherActivitiesOption2));
            otherActivitiesOption3 = otherActivitiesOptionAnonymousValuesList.indexOf(Integer.toString(otherActivitiesOption3));
            otherActivitiesOption4 = otherActivitiesOptionAnonymousValuesList.indexOf(Integer.toString(otherActivitiesOption4));
            otherActivitiesFAB = otherActivitiesFAB >= 9 ? otherActivitiesFAB - 2 : otherActivitiesFAB - 1;
        }

        binding.otherActivitiesFabTextView.setText(fabOptions[otherActivitiesFAB]);

        binding.otherActivitiesOptionCountLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_bottom_app_bar_option_count_options, otherActivitiesOptionCount / 2 - 1, (dialogInterface, i) -> {
                        otherActivitiesOptionCount = (i + 1) * 2;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_COUNT, otherActivitiesOptionCount).apply();
                        binding.otherActivitiesOptionCountTextView.setText(Integer.toString(otherActivitiesOptionCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesOption1LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_1)
                    .setSingleChoiceItems(accountName == null ? otherActivitiesOptionAnonymous : otherActivitiesOptions, otherActivitiesOption1, (dialogInterface, i) -> {
                        otherActivitiesOption1 = i;
                        int optionToSaveToPreference = accountName == null ? Integer.parseInt(otherActivitiesOptionAnonymousValues[i]) : otherActivitiesOption1;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_1, optionToSaveToPreference).apply();
                        binding.otherActivitiesOption1TextView.setText(otherActivitiesOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesOption2LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_2)
                    .setSingleChoiceItems(accountName == null ? otherActivitiesOptionAnonymous : otherActivitiesOptions, otherActivitiesOption2, (dialogInterface, i) -> {
                        otherActivitiesOption2 = i;
                        int optionToSaveToPreference = accountName == null ? Integer.parseInt(otherActivitiesOptionAnonymousValues[i]) : otherActivitiesOption2;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_2, optionToSaveToPreference).apply();
                        binding.otherActivitiesOption2TextView.setText(otherActivitiesOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesOption3LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_3)
                    .setSingleChoiceItems(accountName == null ? otherActivitiesOptionAnonymous : otherActivitiesOptions, otherActivitiesOption3, (dialogInterface, i) -> {
                        otherActivitiesOption3 = i;
                        int optionToSaveToPreference = accountName == null ? Integer.parseInt(otherActivitiesOptionAnonymousValues[i]) : otherActivitiesOption3;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_3, optionToSaveToPreference).apply();
                        binding.otherActivitiesOption3TextView.setText(otherActivitiesOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesOption4LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_4)
                    .setSingleChoiceItems(accountName == null ? otherActivitiesOptionAnonymous : otherActivitiesOptions, otherActivitiesOption4, (dialogInterface, i) -> {
                        otherActivitiesOption4 = i;
                        int optionToSaveToPreference = accountName == null ? Integer.parseInt(otherActivitiesOptionAnonymousValues[i]) : otherActivitiesOption4;
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_4, optionToSaveToPreference).apply();
                        binding.otherActivitiesOption4TextView.setText(otherActivitiesOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesFabLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_fab)
                    .setSingleChoiceItems(fabOptions, otherActivitiesFAB, (dialogInterface, i) -> {
                        otherActivitiesFAB = i;
                        int optionToSaveToPreference;
                        if (accountName == null) {
                            if (i >= 7) {
                                optionToSaveToPreference = i + 2;
                            } else {
                                optionToSaveToPreference = i + 1;
                            }
                        } else {
                            optionToSaveToPreference = i;
                        }
                        sharedPreferences.edit().putInt((accountName == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, optionToSaveToPreference).apply();
                        binding.otherActivitiesFabTextView.setText(fabOptions[otherActivitiesFAB]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        return rootView;
    }

    private void applyCustomTheme() {
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        int secondaryTextColor = activity.customThemeWrapper.getSecondaryTextColor();
        int accentColor = activity.customThemeWrapper.getColorAccent();
        binding.infoTextView.setTextColor(secondaryTextColor);
        Drawable infoDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_info_preference_24dp, activity.customThemeWrapper.getPrimaryIconColor());
        binding.infoTextView.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        binding.mainActivityGroupSummaryTextView.setTextColor(accentColor);
        binding.mainActivityOptionCountTitleTextView.setTextColor(primaryTextColor);
        binding.mainActivityOptionCountTextView.setTextColor(secondaryTextColor);
        binding.mainActivityOption1TitleTextView.setTextColor(primaryTextColor);
        binding.mainActivityOption1TextView.setTextColor(secondaryTextColor);
        binding.mainActivityOption2TitleTextView.setTextColor(primaryTextColor);
        binding.mainActivityOption2TextView.setTextColor(secondaryTextColor);
        binding.mainActivityOption3TitleTextView.setTextColor(primaryTextColor);
        binding.mainActivityOption3TextView.setTextColor(secondaryTextColor);
        binding.mainActivityOption4TitleTextView.setTextColor(primaryTextColor);
        binding.mainActivityOption4TextView.setTextColor(secondaryTextColor);
        binding.mainActivityFabTitleTextView.setTextColor(primaryTextColor);
        binding.mainActivityFabTextView.setTextColor(secondaryTextColor);

        binding.otherActivitiesGroupSummaryTextView.setTextColor(accentColor);
        binding.otherActivitiesOptionCountTitleTextView.setTextColor(primaryTextColor);
        binding.otherActivitiesOptionCountTextView.setTextColor(secondaryTextColor);
        binding.otherActivitiesOption1TitleTextView.setTextColor(primaryTextColor);
        binding.otherActivitiesOption1TextView.setTextColor(secondaryTextColor);
        binding.otherActivitiesOption2TitleTextView.setTextColor(primaryTextColor);
        binding.otherActivitiesOption2TextView.setTextColor(secondaryTextColor);
        binding.otherActivitiesOption3TitleTextView.setTextColor(primaryTextColor);
        binding.otherActivitiesOption3TextView.setTextColor(secondaryTextColor);
        binding.otherActivitiesOption4TitleTextView.setTextColor(primaryTextColor);
        binding.otherActivitiesOption4TextView.setTextColor(secondaryTextColor);
        binding.otherActivitiesFabTitleTextView.setTextColor(primaryTextColor);
        binding.otherActivitiesFabTextView.setTextColor(secondaryTextColor);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}