package ml.docilealligator.infinityforreddit.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.MultiredditSelectionActivity;
import ml.docilealligator.infinityforreddit.activities.SearchActivity;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.activities.SubredditSelectionActivity;
import ml.docilealligator.infinityforreddit.databinding.FragmentCustomizeMainPageTabsBinding;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomizeMainPageTabsFragment extends Fragment {
    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    private FragmentCustomizeMainPageTabsBinding binding;

    @Inject
    @Named("main_activity_tabs")
    SharedPreferences mainActivityTabsSharedPreferences;
    private SettingsActivity activity;
    private String accountName;
    private int tabCount;
    private String tab1CurrentTitle;
    private int tab1CurrentPostType;
    private String tab1CurrentName;
    private String tab2CurrentTitle;
    private int tab2CurrentPostType;
    private String tab2CurrentName;
    private String tab3CurrentTitle;
    private int tab3CurrentPostType;
    private String tab3CurrentName;

    public CustomizeMainPageTabsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCustomizeMainPageTabsBinding.inflate(getLayoutInflater(), container, false);
        View rootView = binding.getRoot();

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        rootView.setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());
        applyCustomTheme();

        accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        String[] typeValues;
        if (accountName == null) {
            typeValues = activity.getResources().getStringArray(R.array.settings_tab_post_type_anonymous);
        } else {
            typeValues = activity.getResources().getStringArray(R.array.settings_tab_post_type);
        }

        tabCount = mainActivityTabsSharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, 3);
        binding.tabCountTextView.setText(Integer.toString(tabCount));
        binding.tabCountLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_main_page_tab_count, tabCount - 1, (dialogInterface, i) -> {
                        tabCount = i + 1;
                        mainActivityTabsSharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, tabCount).apply();
                        binding.tabCountTextView.setText(Integer.toString(tabCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        boolean showTabNames = mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, true);
        binding.showTabNamesSwitch.setChecked(showTabNames);
        binding.showTabNamesSwitch.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, b).apply());
        binding.showTabNamesLinearLayout.setOnClickListener(view -> binding.showTabNamesSwitch.performClick());

        tab1CurrentTitle = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, getString(R.string.home));
        tab1CurrentPostType = mainActivityTabsSharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME);
        tab1CurrentName = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, "");
        binding.tab1TypeSummaryTextView.setText(typeValues[tab1CurrentPostType]);
        binding.tab1TitleSummaryTextView.setText(tab1CurrentTitle);
        binding.tab1NameSummaryTextView.setText(tab1CurrentName);
        applyTab1NameView(binding.tab1NameConstraintLayout, binding.tab1NameTitleTextView, tab1CurrentPostType);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.edit_text_edit_text_dialog);

        binding.tab1TitleLinearLayout.setOnClickListener(view -> {
            editText.setHint(R.string.settings_tab_title);
            editText.setText(tab1CurrentTitle);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab1CurrentTitle = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, tab1CurrentTitle).apply();
                        binding.tab1TitleSummaryTextView.setText(tab1CurrentTitle);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab1TypeLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setSingleChoiceItems(typeValues, tab1CurrentPostType, (dialogInterface, i) -> {
                        tab1CurrentPostType = i;
                        mainActivityTabsSharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, i).apply();
                        binding.tab1TypeSummaryTextView.setText(typeValues[i]);
                        applyTab1NameView(binding.tab1NameConstraintLayout, binding.tab1NameTitleTextView, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        binding.tab1NameConstraintLayout.setOnClickListener(view -> {
            int titleId;
            switch (tab1CurrentPostType) {
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                    titleId = R.string.settings_tab_subreddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                    titleId = R.string.settings_tab_multi_reddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                    titleId = R.string.settings_tab_username;
                    break;
                default:
                    return;
            }
            editText.setText(tab1CurrentName);
            editText.setHint(titleId);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(titleId)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab1CurrentName = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                        binding.tab1NameSummaryTextView.setText(tab1CurrentName);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab1NameAddImageView.setOnClickListener(view -> selectName(0));

        tab2CurrentTitle = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, getString(R.string.popular));
        tab2CurrentPostType = mainActivityTabsSharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR);
        tab2CurrentName = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, "");
        binding.tab2TypeSummaryTextView.setText(typeValues[tab2CurrentPostType]);
        binding.tab2TitleSummaryTextView.setText(tab2CurrentTitle);
        binding.tab2NameSummaryTextView.setText(tab2CurrentName);
        applyTab2NameView(binding.tab2NameConstraintLayout, binding.tab2NameTitleTextView, tab2CurrentPostType);

        binding.tab2TitleLinearLayout.setOnClickListener(view -> {
            editText.setHint(R.string.settings_tab_title);
            editText.setText(tab2CurrentTitle);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab2CurrentTitle = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, tab2CurrentTitle).apply();
                        binding.tab2TitleSummaryTextView.setText(tab2CurrentTitle);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab2TypeLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setSingleChoiceItems(typeValues, tab2CurrentPostType, (dialogInterface, i) -> {
                        tab2CurrentPostType = i;
                        mainActivityTabsSharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, i).apply();
                        binding.tab2TypeSummaryTextView.setText(typeValues[i]);
                        applyTab2NameView(binding.tab2NameConstraintLayout, binding.tab2NameTitleTextView, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        binding.tab2NameConstraintLayout.setOnClickListener(view -> {
            int titleId;
            switch (tab2CurrentPostType) {
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                    titleId = R.string.settings_tab_subreddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                    titleId = R.string.settings_tab_multi_reddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                    titleId = R.string.settings_tab_username;
                    break;
                default:
                    return;
            }
            editText.setText(tab2CurrentName);
            editText.setHint(titleId);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(titleId)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab2CurrentName = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                        binding.tab2NameSummaryTextView.setText(tab2CurrentName);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab2NameAddImageView.setOnClickListener(view -> selectName(1));

        tab3CurrentTitle = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, getString(R.string.all));
        tab3CurrentPostType = mainActivityTabsSharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
        tab3CurrentName = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, "");
        binding.tab3TypeSummaryTextView.setText(typeValues[tab3CurrentPostType]);
        binding.tab3TitleSummaryTextView.setText(tab3CurrentTitle);
        binding.tab3NameSummaryTextView.setText(tab3CurrentName);
        applyTab3NameView(binding.tab3NameConstraintLayout, binding.tab3NameTitleTextView, tab3CurrentPostType);

        binding.tab3TitleLinearLayout.setOnClickListener(view -> {
            editText.setHint(R.string.settings_tab_title);
            editText.setText(tab3CurrentTitle);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab3CurrentTitle = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, tab3CurrentTitle).apply();
                        binding.tab3TitleSummaryTextView.setText(tab3CurrentTitle);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab3TypeLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setSingleChoiceItems(typeValues, tab3CurrentPostType, (dialogInterface, i) -> {
                        tab3CurrentPostType = i;
                        mainActivityTabsSharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, i).apply();
                        binding.tab3TypeSummaryTextView.setText(typeValues[i]);
                        applyTab3NameView(binding.tab3NameConstraintLayout, binding.tab3NameTitleTextView, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        binding.tab3NameConstraintLayout.setOnClickListener(view -> {
            int titleId;
            switch (tab3CurrentPostType) {
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                    titleId = R.string.settings_tab_subreddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                    titleId = R.string.settings_tab_multi_reddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                    titleId = R.string.settings_tab_username;
                    break;
                default:
                    return;
            }
            editText.setText(tab3CurrentName);
            editText.setHint(titleId);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(titleId)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab3CurrentName = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                        binding.tab3NameSummaryTextView.setText(tab3CurrentName);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab3NameAddImageView.setOnClickListener(view -> selectName(2));

        binding.showMultiredditsSwitch.setChecked(mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_MULTIREDDITS, false));
        binding.showMultiredditsSwitch.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_MULTIREDDITS, b).apply());
        binding.showMultiredditsLinearLayout.setOnClickListener(view -> {
            binding.showMultiredditsSwitch.performClick();
        });

        binding.showFavoriteMultiredditsSwitch.setChecked(mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_MULTIREDDITS, false));
        binding.showFavoriteMultiredditsSwitch.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_MULTIREDDITS, b).apply());
        binding.showFavoriteMultiredditsLinearLayout.setOnClickListener(view -> {
            binding.showFavoriteMultiredditsSwitch.performClick();
        });

        binding.showSubscribedSubredditsSwitch.setChecked(mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, false));
        binding.showSubscribedSubredditsSwitch.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, b).apply());
        binding.showSubscribedSubredditsLinearLayout.setOnClickListener(view -> {
            binding.showSubscribedSubredditsSwitch.performClick();
        });

        binding.showFavoriteSubscribedSubredditsSwitch.setChecked(mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, false));
        binding.showFavoriteSubscribedSubredditsSwitch.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, b).apply());
        binding.showFavoriteSubscribedSubredditsLinearLayout.setOnClickListener(view -> {
            binding.showFavoriteSubscribedSubredditsSwitch.performClick();
        });

        return rootView;
    }

    private void applyCustomTheme() {
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        int secondaryTextColor = activity.customThemeWrapper.getSecondaryTextColor();
        int colorAccent = activity.customThemeWrapper.getColorAccent();
        int primaryIconColor = activity.customThemeWrapper.getPrimaryIconColor();
        binding.infoTextView.setTextColor(secondaryTextColor);
        Drawable infoDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_info_preference_24dp, secondaryTextColor);
        binding.infoTextView.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        binding.tabCountTitleTextView.setTextColor(primaryTextColor);
        binding.tabCountTextView.setTextColor(secondaryTextColor);
        binding.showTabNamesTitleTextView.setTextColor(primaryTextColor);
        binding.tab1GroupSummaryTextView.setTextColor(colorAccent);
        binding.tab1TitleTitleTextView.setTextColor(primaryTextColor);
        binding.tab1TitleSummaryTextView.setTextColor(secondaryTextColor);
        binding.tab1TypeTitleTextView.setTextColor(primaryTextColor);
        binding.tab1TypeSummaryTextView.setTextColor(secondaryTextColor);
        binding.tab1NameTitleTextView.setTextColor(primaryTextColor);
        binding.tab1NameSummaryTextView.setTextColor(secondaryTextColor);
        binding.tab1NameAddImageView.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.tab2GroupSummaryTextView.setTextColor(colorAccent);
        binding.tab2TitleTitleTextView.setTextColor(primaryTextColor);
        binding.tab2TitleSummaryTextView.setTextColor(secondaryTextColor);
        binding.tab2TypeTitleTextView.setTextColor(primaryTextColor);
        binding.tab2TypeSummaryTextView.setTextColor(secondaryTextColor);
        binding.tab2NameTitleTextView.setTextColor(primaryTextColor);
        binding.tab2NameSummaryTextView.setTextColor(secondaryTextColor);
        binding.tab2NameAddImageView.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.tab3GroupSummaryTextView.setTextColor(colorAccent);
        binding.tab3TitleTitleTextView.setTextColor(primaryTextColor);
        binding.tab3TitleSummaryTextView.setTextColor(secondaryTextColor);
        binding.tab3TypeTitleTextView.setTextColor(primaryTextColor);
        binding.tab3TypeSummaryTextView.setTextColor(secondaryTextColor);
        binding.tab3NameTitleTextView.setTextColor(primaryTextColor);
        binding.tab3NameSummaryTextView.setTextColor(secondaryTextColor);
        binding.tab3NameAddImageView.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.moreTabsGroupSummaryTextView.setTextColor(colorAccent);
        binding.moreTabsInfoTextView.setTextColor(secondaryTextColor);
        binding.moreTabsInfoTextView.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        binding.showFavoriteMultiredditsTitleTextView.setTextColor(primaryTextColor);
        binding.showMultiredditsTitleTextView.setTextColor(primaryTextColor);
        binding.showSubscribedSubredditsTitleTextView.setTextColor(primaryTextColor);
        binding.showFavoriteSubscribedSubredditsTitleTextView.setTextColor(primaryTextColor);
    }

    private void applyTab1NameView(ConstraintLayout constraintLayout, TextView titleTextView, int postType) {
        switch (postType) {
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_subreddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_multi_reddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_username);
                break;
            default:
                constraintLayout.setVisibility(View.GONE);
        }
    }

    private void applyTab2NameView(ConstraintLayout linearLayout, TextView titleTextView, int postType) {
        switch (postType) {
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                linearLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_subreddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                linearLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_multi_reddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                linearLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_username);
                break;
            default:
                linearLayout.setVisibility(View.GONE);
        }
    }

    private void applyTab3NameView(ConstraintLayout constraintLayout, TextView titleTextView, int postType) {
        switch (postType) {
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_subreddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_multi_reddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_username);
                break;
            default:
                constraintLayout.setVisibility(View.GONE);
        }
    }

    private void selectName(int tab) {
        switch (tab) {
            case 0:
                switch (tab1CurrentPostType) {
                    case 3: {
                        Intent intent = new Intent(activity, SubredditSelectionActivity.class);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 4: {
                        Intent intent = new Intent(activity, MultiredditSelectionActivity.class);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 5: {
                        Intent intent = new Intent(activity, SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
                        startActivityForResult(intent, tab);
                        break;
                    }
                }
                break;
            case 1:
                switch (tab2CurrentPostType) {
                    case 3: {
                        Intent intent = new Intent(activity, SubredditSelectionActivity.class);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 4: {
                        Intent intent = new Intent(activity, MultiredditSelectionActivity.class);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 5: {
                        Intent intent = new Intent(activity, SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
                        startActivityForResult(intent, tab);
                        break;
                    }
                }
                break;
            case 2:
                switch (tab3CurrentPostType) {
                    case 3: {
                        Intent intent = new Intent(activity, SubredditSelectionActivity.class);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 4: {
                        Intent intent = new Intent(activity, MultiredditSelectionActivity.class);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 5: {
                        Intent intent = new Intent(activity, SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
                        startActivityForResult(intent, tab);
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case 0:
                    if (data.hasExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME)) {
                        tab1CurrentName = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                        binding.tab1NameSummaryTextView.setText(tab1CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                    } else if (data.hasExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT)) {
                        MultiReddit multireddit = data.getParcelableExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT);
                        if (multireddit != null) {
                            tab1CurrentName = multireddit.getPath();
                            binding.tab1NameSummaryTextView.setText(tab1CurrentName);
                            mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                        }
                    } else if (data.hasExtra(SearchActivity.EXTRA_RETURN_USER_NAME)) {
                        tab1CurrentName = data.getStringExtra(SearchActivity.EXTRA_RETURN_USER_NAME);
                        binding.tab1NameSummaryTextView.setText(tab1CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                    }
                    break;
                case 1:
                    if (data.hasExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME)) {
                        tab2CurrentName = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                        binding.tab2NameSummaryTextView.setText(tab2CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                    } else if (data.hasExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT)) {
                        MultiReddit multireddit = data.getParcelableExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT);
                        if (multireddit != null) {
                            tab2CurrentName = multireddit.getPath();
                            binding.tab2NameSummaryTextView.setText(tab2CurrentName);
                            mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                        }
                    } else if (data.hasExtra(SearchActivity.EXTRA_RETURN_USER_NAME)) {
                        tab2CurrentName = data.getStringExtra(SearchActivity.EXTRA_RETURN_USER_NAME);
                        binding.tab2NameSummaryTextView.setText(tab2CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                    }
                    break;
                case 2:
                    if (data.hasExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME)) {
                        tab3CurrentName = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                        binding.tab3NameSummaryTextView.setText(tab3CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                    } else if (data.hasExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT)) {
                        MultiReddit multireddit = data.getParcelableExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT);
                        if (multireddit != null) {
                            tab3CurrentName = multireddit.getPath();
                            binding.tab3NameSummaryTextView.setText(tab3CurrentName);
                            mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                        }
                    } else if (data.hasExtra(SearchActivity.EXTRA_RETURN_USER_NAME)) {
                        tab3CurrentName = data.getStringExtra(SearchActivity.EXTRA_RETURN_USER_NAME);
                        binding.tab3NameSummaryTextView.setText(tab3CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                    }
                    break;
            }
        }
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