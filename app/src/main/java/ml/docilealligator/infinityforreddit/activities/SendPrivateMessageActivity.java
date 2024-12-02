package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivitySendPrivateMessageBinding;
import ml.docilealligator.infinityforreddit.message.ComposeMessage;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class SendPrivateMessageActivity extends BaseActivity {
    public static final String EXTRA_RECIPIENT_USERNAME = "ERU";

    private ActivitySendPrivateMessageBinding binding;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String mAccessToken;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();
        
        super.onCreate(savedInstanceState);
        binding = ActivitySendPrivateMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appBarLayout);
        }

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        setSupportActionBar(binding.toolbar);

        String username = getIntent().getStringExtra(EXTRA_RECIPIENT_USERNAME);
        if (username != null) {
            binding.usernameEditText.setText(username);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_private_message_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_send_send_private_message_activity) {
            if (!isSubmitting) {
                isSubmitting = true;
                if (binding.usernameEditText.getText() == null || binding.usernameEditText.getText().toString().equals("")) {
                    isSubmitting = false;
                    Snackbar.make(binding.coordinatorLayout, R.string.message_username_required, Snackbar.LENGTH_LONG).show();
                    return true;
                }

                if (binding.subjectEditText.getText() == null || binding.subjectEditText.getText().toString().equals("")) {
                    isSubmitting = false;
                    Snackbar.make(binding.coordinatorLayout, R.string.message_subject_required, Snackbar.LENGTH_LONG).show();
                    return true;
                }

                if (binding.contentEditText.getText() == null || binding.contentEditText.getText().toString().equals("")) {
                    isSubmitting = false;
                    Snackbar.make(binding.coordinatorLayout, R.string.message_content_required, Snackbar.LENGTH_LONG).show();
                    return true;
                }

                item.setEnabled(false);
                item.getIcon().setAlpha(130);
                Snackbar sendingSnackbar = Snackbar.make(binding.coordinatorLayout, R.string.sending_message, Snackbar.LENGTH_INDEFINITE);
                sendingSnackbar.show();

                ComposeMessage.composeMessage(mOauthRetrofit, mAccessToken, getResources().getConfiguration().locale,
                        binding.usernameEditText.getText().toString(), binding.subjectEditText.getText().toString(),
                        binding.contentEditText.getText().toString(), new ComposeMessage.ComposeMessageListener() {
                            @Override
                            public void composeMessageSuccess() {
                                isSubmitting = false;
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);
                                Toast.makeText(SendPrivateMessageActivity.this, R.string.send_message_success, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void composeMessageFailed(String errorMessage) {
                                isSubmitting = false;
                                sendingSnackbar.dismiss();
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);

                                if (errorMessage == null || errorMessage.equals("")) {
                                    Snackbar.make(binding.coordinatorLayout, R.string.send_message_failed, Snackbar.LENGTH_LONG).show();
                                } else {
                                    Snackbar.make(binding.coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
                binding.appBarLayout, null, binding.toolbar);
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        binding.usernameEditText.setTextColor(primaryTextColor);
        binding.subjectEditText.setTextColor(primaryTextColor);
        binding.contentEditText.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.usernameEditText.setHintTextColor(secondaryTextColor);
        binding.subjectEditText.setHintTextColor(secondaryTextColor);
        binding.contentEditText.setHintTextColor(secondaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1.setBackgroundColor(dividerColor);
        binding.divider2.setBackgroundColor(dividerColor);
        if (typeface != null) {
            binding.usernameEditText.setTypeface(typeface);
            binding.subjectEditText.setTypeface(typeface);
            binding.contentEditText.setTypeface(typeface);
        }
    }
}