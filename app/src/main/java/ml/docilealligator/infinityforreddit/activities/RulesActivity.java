package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.FetchRules;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Rule;
import ml.docilealligator.infinityforreddit.adapters.RulesRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.customviews.slidr.widget.SliderPanel;
import ml.docilealligator.infinityforreddit.databinding.ActivityRulesBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class RulesActivity extends BaseActivity {
    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private ActivityRulesBinding binding;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    private String mAccessToken;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mSubredditName;
    private RulesRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivityRulesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        SliderPanel sliderPanel = null;
        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            sliderPanel = Slidr.attach(this);
        }

        if (isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appBarLayout);
        }

        Window window = getWindow();
        if (isImmersiveInterface()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
            } else {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
            adjustToolbar(binding.toolbar);

            int navBarHeight = getNavBarHeight();
            if (navBarHeight > 0) {
                binding.recyclerView.setPadding(0, 0, 0, navBarHeight);
            }
        }
        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        binding.appBarLayout.setBackgroundColor(mCustomThemeWrapper.getColorPrimary());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSubredditName = getIntent().getExtras().getString(EXTRA_SUBREDDIT_NAME);

        mAdapter = new RulesRecyclerViewAdapter(this, mCustomThemeWrapper, sliderPanel);
        binding.recyclerView.setAdapter(mAdapter);

        FetchRules.fetchRules(mExecutor, new Handler(), mAccessToken == null ? mRetrofit : mOauthRetrofit, mAccessToken, mSubredditName, new FetchRules.FetchRulesListener() {
            @Override
            public void success(ArrayList<Rule> rules) {
                binding.progressBar.setVisibility(View.GONE);
                if (rules == null || rules.size() == 0) {
                    binding.errorTextView.setVisibility(View.VISIBLE);
                    binding.errorTextView.setText(R.string.no_rule);
                    binding.errorTextView.setOnClickListener(view -> {
                    });
                }
                mAdapter.changeDataset(rules);
            }

            @Override
            public void failed() {
                displayError();
            }
        });

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
        binding.errorTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
    }

    private void displayError() {
        TextView errorTextView = binding.errorTextView;
        binding.progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(R.string.error_loading_rules);
        errorTextView.setOnClickListener(view -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            errorTextView.setVisibility(View.GONE);
            FetchRules.fetchRules(mExecutor, new Handler(), mAccessToken == null ? mRetrofit : mOauthRetrofit, mAccessToken, mSubredditName, new FetchRules.FetchRulesListener() {
                @Override
                public void success(ArrayList<Rule> rules) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (rules == null || rules.size() == 0) {
                        errorTextView.setVisibility(View.VISIBLE);
                        errorTextView.setText(R.string.no_rule);
                        errorTextView.setOnClickListener(view -> {
                        });
                    }
                    mAdapter.changeDataset(rules);
                }

                @Override
                public void failed() {
                    displayError();
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}
