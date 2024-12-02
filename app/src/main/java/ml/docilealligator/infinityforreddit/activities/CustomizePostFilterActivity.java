package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityCustomizePostFilterBinding;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.SavePostFilter;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomizePostFilterActivity extends BaseActivity {
    public static final String EXTRA_POST_FILTER = "EPF";
    public static final String EXTRA_FROM_SETTINGS = "EFS";
    public static final String EXTRA_EXCLUDE_SUBREDDIT = "EES";
    public static final String EXTRA_EXCLUDE_USER = "EEU";
    public static final String EXTRA_EXCLUDE_FLAIR = "EEF";
    public static final String EXTRA_CONTAIN_FLAIR = "ECF";
    public static final String EXTRA_EXCLUDE_DOMAIN = "EED";
    public static final String EXTRA_CONTAIN_DOMAIN = "ECD";
    public static final String RETURN_EXTRA_POST_FILTER = "REPF";
    private static final String POST_FILTER_STATE = "PFS";
    private static final String ORIGINAL_NAME_STATE = "ONS";
    private static final int ADD_SUBREDDITS_REQUEST_CODE = 1;
    private static final int ADD_SUBREDDITS_ANONYMOUS_REQUEST_CODE = 2;
    private static final int ADD_USERS_REQUEST_CODE = 3;

    private ActivityCustomizePostFilterBinding binding;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences currentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private PostFilter postFilter;
    private boolean fromSettings;
    private String originalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityCustomizePostFilterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appBarLayout);
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbar);

        fromSettings = getIntent().getBooleanExtra(EXTRA_FROM_SETTINGS, false);

        binding.postTypeTextLinearLayout.setOnClickListener(view -> {
            binding.postTypeTextCheckBox.performClick();
        });

        binding.postTypeLinkLinearLayout.setOnClickListener(view -> {
            binding.postTypeLinkCheckBox.performClick();
        });

        binding.postTypeImageLinearLayout.setOnClickListener(view -> {
            binding.postTypeImageCheckBox.performClick();
        });

        binding.postTypeGifLinearLayout.setOnClickListener(view -> {
            binding.postTypeGifCheckBox.performClick();
        });

        binding.postTypeVideoLinearLayout.setOnClickListener(view -> {
            binding.postTypeVideoCheckBox.performClick();
        });

        binding.postTypeGalleryLinearLayout.setOnClickListener(view -> {
            binding.postTypeGalleryCheckBox.performClick();
        });

        binding.onlyNsfwLinearLayout.setOnClickListener(view -> {
            binding.onlyNsfwSwitch.performClick();
        });

        binding.onlySpoilerLinearLayout.setOnClickListener(view -> {
            binding.onlySpoilerSwitch.performClick();
        });

        String accessToken = currentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        binding.addSubredditsImageView.setOnClickListener(view -> {
            if (accessToken == null) {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, true);
                intent.putExtra(SearchActivity.EXTRA_IS_MULTI_SELECTION, true);
                startActivityForResult(intent, ADD_SUBREDDITS_ANONYMOUS_REQUEST_CODE);
            } else {
                Intent intent = new Intent(this, SubredditMultiselectionActivity.class);
                startActivityForResult(intent, ADD_SUBREDDITS_REQUEST_CODE);
            }
        });

        binding.addUsersImageView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
            intent.putExtra(SearchActivity.EXTRA_IS_MULTI_SELECTION, true);
            startActivityForResult(intent, ADD_USERS_REQUEST_CODE);
        });

        if (savedInstanceState != null) {
            postFilter = savedInstanceState.getParcelable(POST_FILTER_STATE);
            originalName = savedInstanceState.getString(ORIGINAL_NAME_STATE);
        } else {
            postFilter = getIntent().getParcelableExtra(EXTRA_POST_FILTER);
            if (postFilter == null) {
                postFilter = new PostFilter();
                originalName = "";
            } else {
                if (!fromSettings) {
                    originalName = "";
                } else {
                    originalName = postFilter.name;
                }
            }
            bindView();
        }
    }

    private void bindView() {
        binding.nameTextInputEditText.setText(postFilter.name);
        binding.postTypeTextCheckBox.setChecked(postFilter.containTextType);
        binding.postTypeLinkCheckBox.setChecked(postFilter.containLinkType);
        binding.postTypeImageCheckBox.setChecked(postFilter.containImageType);
        binding.postTypeGifCheckBox.setChecked(postFilter.containGifType);
        binding.postTypeVideoCheckBox.setChecked(postFilter.containVideoType);
        binding.postTypeGalleryCheckBox.setChecked(postFilter.containGalleryType);
        binding.onlyNsfwSwitch.setChecked(postFilter.onlyNSFW);
        binding.onlySpoilerSwitch.setChecked(postFilter.onlySpoiler);
        binding.titleExcludesStringsTextInputEditText.setText(postFilter.postTitleExcludesStrings);
        binding.titleContainsStringsTextInputEditText.setText(postFilter.postTitleContainsStrings);
        binding.titleExcludesRegexTextInputEditText.setText(postFilter.postTitleExcludesRegex);
        binding.titleContainsRegexTextInputEditText.setText(postFilter.postTitleContainsRegex);
        binding.excludesSubredditsTextInputEditText.setText(postFilter.excludeSubreddits);
        binding.excludesUsersTextInputEditText.setText(postFilter.excludeUsers);
        binding.excludesFlairsTextInputEditText.setText(postFilter.excludeFlairs);
        binding.containsFlairsTextInputEditText.setText(postFilter.containFlairs);
        binding.excludeDomainsTextInputEditText.setText(postFilter.excludeDomains);
        binding.containDomainsTextInputEditText.setText(postFilter.containDomains);
        binding.minVoteTextInputEditText.setText(Integer.toString(postFilter.minVote));
        binding.maxVoteTextInputEditText.setText(Integer.toString(postFilter.maxVote));
        binding.minCommentsTextInputEditText.setText(Integer.toString(postFilter.minComments));
        binding.maxCommentsTextInputEditText.setText(Integer.toString(postFilter.maxComments));
        binding.minAwardsTextInputEditText.setText(Integer.toString(postFilter.minAwards));
        binding.maxAwardsTextInputEditText.setText(Integer.toString(postFilter.maxAwards));

        Intent intent = getIntent();
        String excludeSubreddit = intent.getStringExtra(EXTRA_EXCLUDE_SUBREDDIT);
        String excludeUser = intent.getStringExtra(EXTRA_EXCLUDE_USER);
        String excludeFlair = intent.getStringExtra(EXTRA_EXCLUDE_FLAIR);
        String containFlair = intent.getStringExtra(EXTRA_CONTAIN_FLAIR);
        String excludeDomain = intent.getStringExtra(EXTRA_EXCLUDE_DOMAIN);
        String containDomain = intent.getStringExtra(EXTRA_CONTAIN_DOMAIN);

        if (excludeSubreddit != null && !excludeSubreddit.equals("")) {
            if (!binding.excludesSubredditsTextInputEditText.getText().toString().equals("")) {
                binding.excludesSubredditsTextInputEditText.append(",");
            }
            binding.excludesSubredditsTextInputEditText.append(excludeSubreddit);
        }
        if (excludeUser != null && !excludeUser.equals("")) {
            if (!binding.excludesUsersTextInputEditText.getText().toString().equals("")) {
                binding.excludesUsersTextInputEditText.append(",");
            }
            binding.excludesUsersTextInputEditText.append(excludeUser);
        }
        if (excludeFlair != null && !excludeFlair.equals("")) {
            if (!binding.excludesFlairsTextInputEditText.getText().toString().equals("")) {
                binding.excludesFlairsTextInputEditText.append(",");
            }
            binding.excludesFlairsTextInputEditText.append(excludeFlair);
        }
        if (containFlair != null && !containFlair.equals("")) {
            if (!binding.containsFlairsTextInputEditText.getText().toString().equals("")) {
                binding.containsFlairsTextInputEditText.append(",");
            }
            binding.containsFlairsTextInputEditText.append(containFlair);
        }
        if (excludeDomain != null && !excludeDomain.equals("")) {
            if (!binding.excludeDomainsTextInputEditText.getText().toString().equals("")) {
                binding.excludeDomainsTextInputEditText.append(",");
            }
            binding.excludeDomainsTextInputEditText.append(Uri.parse(excludeDomain).getHost());
        }
        if (containDomain != null && !containDomain.equals("")) {
            if (!binding.containDomainsTextInputEditText.getText().toString().equals("")) {
                binding.containDomainsTextInputEditText.append(",");
            }
            binding.containDomainsTextInputEditText.append(Uri.parse(containDomain).getHost());
        }
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
        binding.coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(
                binding.appBarLayout, binding.collapsingToolbarLayout, binding.toolbar);
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        int primaryIconColor = mCustomThemeWrapper.getPrimaryIconColor();
        Drawable cursorDrawable = Utils.getTintedDrawable(this, R.drawable.edit_text_cursor, primaryTextColor);
        binding.nameTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.nameTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.nameTextInputEditText.setTextColor(primaryTextColor);
        binding.postTypeTextTextView.setTextColor(primaryTextColor);
        binding.postTypeLinkTextView.setTextColor(primaryTextColor);
        binding.postTypeImageTextView.setTextColor(primaryTextColor);
        binding.postTypeGifTextView.setTextColor(primaryTextColor);
        binding.postTypeVideoTextView.setTextColor(primaryTextColor);
        binding.postTypeGalleryTextView.setTextColor(primaryTextColor);
        binding.onlyNsfwSwitch.setTextColor(primaryTextColor);
        binding.onlySpoilerTextView.setTextColor(primaryTextColor);
        binding.titleExcludesStringsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.titleExcludesStringsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.titleExcludesStringsTextInputEditText.setTextColor(primaryTextColor);
        binding.titleContainsStringsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.titleContainsStringsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.titleContainsStringsTextInputEditText.setTextColor(primaryTextColor);
        binding.titleExcludesRegexTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.titleExcludesRegexTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.titleExcludesRegexTextInputEditText.setTextColor(primaryTextColor);
        binding.titleContainsRegexTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.titleContainsRegexTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.titleContainsRegexTextInputEditText.setTextColor(primaryTextColor);
        binding.excludesSubredditsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.excludesSubredditsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludesSubredditsTextInputEditText.setTextColor(primaryTextColor);
        binding.addSubredditsImageView.setImageDrawable(Utils.getTintedDrawable(this, R.drawable.ic_add_24dp, primaryIconColor));
        binding.excludesUsersTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.excludesUsersTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludesUsersTextInputEditText.setTextColor(primaryTextColor);
        binding.addUsersImageView.setImageDrawable(Utils.getTintedDrawable(this, R.drawable.ic_add_24dp, primaryIconColor));
        binding.excludesFlairsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.excludesFlairsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludesFlairsTextInputEditText.setTextColor(primaryTextColor);
        binding.containsFlairsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.containsFlairsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.containsFlairsTextInputEditText.setTextColor(primaryTextColor);
        binding.excludeDomainsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.excludeDomainsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludeDomainsTextInputEditText.setTextColor(primaryTextColor);
        binding.containDomainsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.containDomainsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.containDomainsTextInputEditText.setTextColor(primaryTextColor);
        binding.minVoteTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.minVoteTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.minVoteTextInputEditText.setTextColor(primaryTextColor);
        binding.maxVoteTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.maxVoteTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.maxVoteTextInputEditText.setTextColor(primaryTextColor);
        binding.minCommentsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.minCommentsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.minCommentsTextInputEditText.setTextColor(primaryTextColor);
        binding.maxCommentsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.maxCommentsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.maxCommentsTextInputEditText.setTextColor(primaryTextColor);
        binding.minAwardsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.minAwardsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.minAwardsTextInputEditText.setTextColor(primaryTextColor);
        binding.maxAwardsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        binding.maxAwardsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.maxAwardsTextInputEditText.setTextColor(primaryTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.nameTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.titleExcludesStringsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.titleContainsStringsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.titleExcludesRegexTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.titleContainsRegexTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.excludesSubredditsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.excludesUsersTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.excludesFlairsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.containsFlairsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.excludeDomainsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.containDomainsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.minVoteTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.maxVoteTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.minCommentsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.maxCommentsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.minAwardsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            binding.maxAwardsTextInputEditText.setTextCursorDrawable(cursorDrawable);
        } else {
            setCursorDrawableColor(binding.nameTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.titleExcludesStringsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.titleContainsStringsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.titleExcludesRegexTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.titleContainsRegexTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.excludesSubredditsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.excludesUsersTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.excludesFlairsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.containsFlairsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.excludeDomainsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.containDomainsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.minVoteTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.maxVoteTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.minCommentsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.maxCommentsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.minAwardsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(binding.maxAwardsTextInputEditText, primaryTextColor);
        }

        if (typeface != null) {
            Utils.setFontToAllTextViews(binding.coordinatorLayout, typeface);
        }
    }

    public void setCursorDrawableColor(EditText editText, int color) {
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = editText.getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[1] = editText.getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (Throwable ignored) { }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customize_post_filter_activity, menu);
        if (fromSettings) {
            menu.findItem(R.id.action_save_customize_post_filter_activity).setVisible(false);
        }
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save_customize_post_filter_activity) {
            try {
                constructPostFilter();
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RETURN_EXTRA_POST_FILTER, postFilter);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } catch (PatternSyntaxException e) {
                Toast.makeText(this, R.string.invalid_regex, Toast.LENGTH_SHORT).show();
            }

            return true;
        } else if (item.getItemId() == R.id.action_save_to_database_customize_post_filter_activity) {
            try {
                constructPostFilter();

                if (!postFilter.name.equals("")) {
                    savePostFilter(originalName);
                } else {
                    Toast.makeText(CustomizePostFilterActivity.this, R.string.post_filter_requires_a_name, Toast.LENGTH_LONG).show();
                }
            } catch (PatternSyntaxException e) {
                Toast.makeText(this, R.string.invalid_regex, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private void savePostFilter(String originalName) {
        SavePostFilter.savePostFilter(mExecutor, new Handler(), mRedditDataRoomDatabase, postFilter, originalName,
                new SavePostFilter.SavePostFilterListener() {
                    @Override
                    public void success() {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(RETURN_EXTRA_POST_FILTER, postFilter);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }

                    @Override
                    public void duplicate() {
                        new MaterialAlertDialogBuilder(CustomizePostFilterActivity.this, R.style.MaterialAlertDialogTheme)
                                .setTitle(getString(R.string.duplicate_post_filter_dialog_title, postFilter.name))
                                .setMessage(R.string.duplicate_post_filter_dialog_message)
                                .setPositiveButton(R.string.override, (dialogInterface, i) -> savePostFilter(postFilter.name))
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_SUBREDDITS_REQUEST_CODE) {
                ArrayList<String> subredditNames = data.getStringArrayListExtra(SubredditMultiselectionActivity.EXTRA_RETURN_SELECTED_SUBREDDITS);
                updateExcludeSubredditNames(subredditNames);
            } else if (requestCode == ADD_SUBREDDITS_ANONYMOUS_REQUEST_CODE) {
                ArrayList<String> subredditNames = data.getStringArrayListExtra(SearchActivity.RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES);
                updateExcludeSubredditNames(subredditNames);
            } else if (requestCode == ADD_USERS_REQUEST_CODE) {
                ArrayList<String> usernames = data.getStringArrayListExtra(SearchActivity.RETURN_EXTRA_SELECTED_USERNAMES);
                String currentUsers = binding.excludesUsersTextInputEditText.getText().toString().trim();
                if (usernames != null && !usernames.isEmpty()) {
                    if (!currentUsers.isEmpty() && currentUsers.charAt(currentUsers.length() - 1) != ',') {
                        String newString = currentUsers + ",";
                        binding.excludesUsersTextInputEditText.setText(newString);
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String s : usernames) {
                        stringBuilder.append(s).append(",");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    binding.excludesUsersTextInputEditText.append(stringBuilder.toString());
                }
            }
        }
    }

    private void updateExcludeSubredditNames(ArrayList<String> subredditNames) {
        String currentSubreddits = binding.excludesSubredditsTextInputEditText.getText().toString().trim();
        if (subredditNames != null && !subredditNames.isEmpty()) {
            if (!currentSubreddits.isEmpty() && currentSubreddits.charAt(currentSubreddits.length() - 1) != ',') {
                String newString = currentSubreddits + ",";
                binding.excludesSubredditsTextInputEditText.setText(newString);
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : subredditNames) {
                stringBuilder.append(s).append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            binding.excludesSubredditsTextInputEditText.append(stringBuilder.toString());
        }
    }

    private void constructPostFilter() throws PatternSyntaxException {
        postFilter.name = binding.nameTextInputEditText.getText().toString();
        postFilter.maxVote = binding.maxVoteTextInputEditText.getText() == null || binding.maxVoteTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(binding.maxVoteTextInputEditText.getText().toString());
        postFilter.minVote = binding.minVoteTextInputEditText.getText() == null || binding.minVoteTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(binding.minVoteTextInputEditText.getText().toString());
        postFilter.maxComments = binding.maxCommentsTextInputEditText.getText() == null || binding.maxCommentsTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(binding.maxCommentsTextInputEditText.getText().toString());
        postFilter.minComments = binding.minCommentsTextInputEditText.getText() == null || binding.minCommentsTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(binding.minCommentsTextInputEditText.getText().toString());
        postFilter.maxAwards = binding.maxAwardsTextInputEditText.getText() == null || binding.maxAwardsTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(binding.maxAwardsTextInputEditText.getText().toString());
        postFilter.minAwards = binding.minAwardsTextInputEditText.getText() == null || binding.minAwardsTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(binding.minAwardsTextInputEditText.getText().toString());
        postFilter.postTitleExcludesRegex = binding.titleExcludesRegexTextInputEditText.getText().toString();
        Pattern.compile(postFilter.postTitleExcludesRegex);
        postFilter.postTitleContainsRegex = binding.titleContainsRegexTextInputEditText.getText().toString();
        Pattern.compile(postFilter.postTitleContainsRegex);
        postFilter.postTitleExcludesStrings = binding.titleExcludesStringsTextInputEditText.getText().toString();
        postFilter.postTitleContainsStrings = binding.titleContainsStringsTextInputEditText.getText().toString();
        postFilter.excludeSubreddits = binding.excludesSubredditsTextInputEditText.getText().toString();
        postFilter.excludeUsers = binding.excludesUsersTextInputEditText.getText().toString();
        postFilter.excludeFlairs = binding.excludesFlairsTextInputEditText.getText().toString();
        postFilter.containFlairs = binding.containsFlairsTextInputEditText.getText().toString();
        postFilter.excludeDomains = binding.excludeDomainsTextInputEditText.getText().toString();
        postFilter.containDomains = binding.containDomainsTextInputEditText.getText().toString();
        postFilter.containTextType = binding.postTypeTextCheckBox.isChecked();
        postFilter.containLinkType = binding.postTypeLinkCheckBox.isChecked();
        postFilter.containImageType = binding.postTypeImageCheckBox.isChecked();
        postFilter.containGifType = binding.postTypeGifCheckBox.isChecked();
        postFilter.containVideoType = binding.postTypeVideoCheckBox.isChecked();
        postFilter.containGalleryType = binding.postTypeGalleryCheckBox.isChecked();
        postFilter.onlyNSFW = binding.onlyNsfwSwitch.isChecked();
        postFilter.onlySpoiler = binding.onlySpoilerSwitch.isChecked();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(POST_FILTER_STATE, postFilter);
        outState.putString(ORIGINAL_NAME_STATE, originalName);
    }
}