package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.inputmethod.EditorInfoCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityCreateMultiRedditBinding;
import ml.docilealligator.infinityforreddit.multireddit.CreateMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditJSONModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CreateMultiRedditActivity extends BaseActivity {
    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 1;
    private static final String SELECTED_SUBREDDITS_STATE = "SSS";

    private ActivityCreateMultiRedditBinding binding;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
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
    private ArrayList<String> mSubreddits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();
        
        super.onCreate(savedInstanceState);
        binding = ActivityCreateMultiRedditBinding.inflate(getLayoutInflater());
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
            binding.nameEditText.setImeOptions(binding.nameEditText.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
            binding.descriptionEditText.setImeOptions(binding.descriptionEditText.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }

        if (savedInstanceState != null) {
            mSubreddits = savedInstanceState.getStringArrayList(SELECTED_SUBREDDITS_STATE);
        } else {
            mSubreddits = new ArrayList<>();
        }
        bindView();
    }

    private void bindView() {
        binding.selectSubredditTextView.setOnClickListener(view -> {
            Intent intent = new Intent(CreateMultiRedditActivity.this, SelectedSubredditsAndUsersActivity.class);
            intent.putStringArrayListExtra(SelectedSubredditsAndUsersActivity.EXTRA_SELECTED_SUBREDDITS, mSubreddits);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_multi_reddit_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_save_create_multi_reddit_activity) {
            if (binding.nameEditText.getText() == null || binding.nameEditText.getText().toString().equals("")) {
                Snackbar.make(binding.coordinatorLayout, R.string.no_multi_reddit_name, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (mAccessToken != null) {
                String jsonModel = new MultiRedditJSONModel(binding.nameEditText.getText().toString(), binding.descriptionEditText.getText().toString(),
                        binding.visibilitySwitch.isChecked(), mSubreddits).createJSONModel();
                CreateMultiReddit.createMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase, mAccessToken,
                        "/user/" + mAccountName + "/m/" + binding.nameEditText.getText().toString(),
                        jsonModel, new CreateMultiReddit.CreateMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed(int errorCode) {
                                if (errorCode == 409) {
                                    Snackbar.make(binding.coordinatorLayout, R.string.duplicate_multi_reddit, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(binding.coordinatorLayout, R.string.create_multi_reddit_failed, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                CreateMultiReddit.anonymousCreateMultiReddit(mExecutor, new Handler(), mRedditDataRoomDatabase,
                        "/user/-/m/" + binding.nameEditText.getText().toString(),
                        binding.nameEditText.getText().toString(), binding.descriptionEditText.getText().toString(),
                        mSubreddits, new CreateMultiReddit.CreateMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed(int errorType) {
                                //Will not be called
                            }
                        });
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                mSubreddits = data.getStringArrayListExtra(
                        SubredditMultiselectionActivity.EXTRA_RETURN_SELECTED_SUBREDDITS);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SELECTED_SUBREDDITS_STATE, mSubreddits);
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
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.nameEditText.setTextColor(primaryTextColor);
        binding.nameEditText.setHintTextColor(secondaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1.setBackgroundColor(dividerColor);
        binding.divider2.setBackgroundColor(dividerColor);
        binding.descriptionEditText.setTextColor(primaryTextColor);
        binding.descriptionEditText.setHintTextColor(secondaryTextColor);
        binding.visibilityTextView.setTextColor(primaryTextColor);
        binding.selectSubredditTextView.setTextColor(primaryTextColor);

        if (typeface != null) {
            Utils.setFontToAllTextViews(binding.coordinatorLayout, typeface);
        }
    }
}
