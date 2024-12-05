package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.inputmethod.EditorInfoCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityEditMultiRedditBinding;
import ml.docilealligator.infinityforreddit.multireddit.EditMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.FetchMultiRedditInfo;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditJSONModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class EditMultiRedditActivity extends BaseActivity {
    public static final String EXTRA_MULTI_PATH = "EMP";
    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 1;
    private static final String MULTI_REDDIT_STATE = "MRS";
    private static final String MULTI_PATH_STATE = "MPS";
    
    private ActivityEditMultiRedditBinding binding;

    @Inject
    @Named("oauth")
    Retrofit mRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mAccessToken;
    private String mAccountName;
    private MultiReddit multiReddit;
    private String multipath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityEditMultiRedditBinding.inflate(getLayoutInflater());
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

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, "-");

        if (mAccessToken == null) {
            binding.visibilityWrapperLinearLayout.setVisibility(View.GONE);
            binding.multiRedditNameEditText.setImeOptions(binding.multiRedditNameEditText.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
            binding.descriptionEditText.setImeOptions(binding.descriptionEditText.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }

        if (savedInstanceState != null) {
            multiReddit = savedInstanceState.getParcelable(MULTI_REDDIT_STATE);
            multipath = savedInstanceState.getString(MULTI_PATH_STATE);
        } else {
            multipath = getIntent().getStringExtra(EXTRA_MULTI_PATH);
        }

        bindView();
    }

    private void bindView() {
        if (multiReddit == null) {
            if (mAccessToken == null) {
                FetchMultiRedditInfo.anonymousFetchMultiRedditInfo(mExecutor, new Handler(),
                        mRedditDataRoomDatabase, multipath, new FetchMultiRedditInfo.FetchMultiRedditInfoListener() {
                            @Override
                            public void success(MultiReddit multiReddit) {
                                EditMultiRedditActivity.this.multiReddit = multiReddit;
                                binding.progressBar.setVisibility(View.GONE);
                                binding.linearLayout.setVisibility(View.VISIBLE);
                                binding.multiRedditNameEditText.setText(multiReddit.getDisplayName());
                                binding.descriptionEditText.setText(multiReddit.getDescription());
                            }

                            @Override
                            public void failed() {
                                //Will not be called
                            }
                        });
            } else {
                FetchMultiRedditInfo.fetchMultiRedditInfo(mRetrofit, mAccessToken, multipath, new FetchMultiRedditInfo.FetchMultiRedditInfoListener() {
                    @Override
                    public void success(MultiReddit multiReddit) {
                        EditMultiRedditActivity.this.multiReddit = multiReddit;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.linearLayout.setVisibility(View.VISIBLE);
                        binding.multiRedditNameEditText.setText(multiReddit.getDisplayName());
                        binding.descriptionEditText.setText(multiReddit.getDescription());
                        binding.visibilitySwitch.setChecked(!multiReddit.getVisibility().equals("public"));
                    }

                    @Override
                    public void failed() {
                        Snackbar.make(binding.coordinatorLayout, R.string.cannot_fetch_multireddit, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.linearLayout.setVisibility(View.VISIBLE);
            binding.multiRedditNameEditText.setText(multiReddit.getDisplayName());
            binding.descriptionEditText.setText(multiReddit.getDescription());
            binding.visibilitySwitch.setChecked(!multiReddit.getVisibility().equals("public"));
        }

        binding.selectSubredditTextView.setOnClickListener(view -> {
            Intent intent = new Intent(EditMultiRedditActivity.this, SelectedSubredditsAndUsersActivity.class);
            if (multiReddit.getSubreddits() != null) {
                intent.putStringArrayListExtra(SelectedSubredditsAndUsersActivity.EXTRA_SELECTED_SUBREDDITS, multiReddit.getSubreddits());
            }
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_multi_reddit_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_save_edit_multi_reddit_activity) {
            if (binding.multiRedditNameEditText.getText() == null || binding.multiRedditNameEditText.getText().toString().equals("")) {
                Snackbar.make(binding.coordinatorLayout, R.string.no_multi_reddit_name, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (mAccessToken == null) {
                String name = binding.multiRedditNameEditText.getText().toString();
                multiReddit.setDisplayName(name);
                multiReddit.setName(name);
                multiReddit.setDescription(binding.descriptionEditText.getText().toString());
                EditMultiReddit.anonymousEditMultiReddit(mExecutor, new Handler(), mRedditDataRoomDatabase,
                        multiReddit, new EditMultiReddit.EditMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed() {
                                //Will not be called
                            }
                        });
            } else {
                String jsonModel = new MultiRedditJSONModel(binding.multiRedditNameEditText.getText().toString(), binding.descriptionEditText.getText().toString(),
                        binding.visibilitySwitch.isChecked(), multiReddit.getSubreddits()).createJSONModel();
                EditMultiReddit.editMultiReddit(mRetrofit, mAccessToken, multiReddit.getPath(),
                        jsonModel, new EditMultiReddit.EditMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed() {
                                Snackbar.make(binding.coordinatorLayout, R.string.edit_multi_reddit_failed, Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                multiReddit.setSubreddits(data.getStringArrayListExtra(
                        SelectedSubredditsAndUsersActivity.EXTRA_RETURN_SELECTED_SUBREDDITS));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MULTI_REDDIT_STATE, multiReddit);
        outState.putString(MULTI_PATH_STATE, multipath);
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
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
        binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(mCustomThemeWrapper.getColorAccent()));
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.multiRedditNameEditText.setTextColor(primaryTextColor);
        binding.multiRedditNameEditText.setHintTextColor(secondaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1.setBackgroundColor(dividerColor);
        binding.divider2.setBackgroundColor(dividerColor);
        binding.descriptionEditText.setTextColor(primaryTextColor);
        binding.descriptionEditText.setHintTextColor(secondaryTextColor);
        binding.visibilityTextView.setTextColor(primaryTextColor);
        binding.selectSubredditTextView.setTextColor(primaryTextColor);
    }
}
