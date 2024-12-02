package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.asynctasks.InsertMultireddit;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityMultiredditSelectionBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.MultiRedditListingFragment;
import ml.docilealligator.infinityforreddit.multireddit.FetchMyMultiReddits;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class MultiredditSelectionActivity extends BaseActivity implements ActivityToolbarInterface {
    public static final String EXTRA_RETURN_MULTIREDDIT = "ERM";
    private static final String INSERT_SUBSCRIBED_MULTIREDDIT_STATE = "ISSS";
    private static final String FRAGMENT_OUT_STATE = "FOS";

    private ActivityMultiredditSelectionBinding binding;

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
    private boolean mInsertSuccess = false;
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        binding = ActivityMultiredditSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.toolbar);
            }
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        if (savedInstanceState == null) {
            bindView(true);
        } else {
            mInsertSuccess = savedInstanceState.getBoolean(INSERT_SUBSCRIBED_MULTIREDDIT_STATE);
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, mFragment).commit();
            bindView(false);
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
    }

    private void bindView(boolean initializeFragment) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (mAccessToken != null) {
            loadMultiReddits();
        }

        if (initializeFragment) {
            mFragment = new MultiRedditListingFragment();
            Bundle bundle = new Bundle();
            bundle.putString(MultiRedditListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
            bundle.putString(MultiRedditListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
            bundle.putBoolean(MultiRedditListingFragment.EXTRA_IS_GETTING_MULTIREDDIT_INFO, true);
            mFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, mFragment).commit();
        }
    }

    private void loadMultiReddits() {
        if (!mInsertSuccess) {
            FetchMyMultiReddits.fetchMyMultiReddits(mOauthRetrofit, mAccessToken, new FetchMyMultiReddits.FetchMyMultiRedditsListener() {
                @Override
                public void success(ArrayList<MultiReddit> multiReddits) {
                    InsertMultireddit.insertMultireddits(mExecutor, new Handler(), mRedditDataRoomDatabase,
                            multiReddits, mAccountName, () -> {
                        mInsertSuccess = true;
                        ((MultiRedditListingFragment) mFragment).stopRefreshProgressbar();
                    });
                }

                @Override
                public void failed() {
                    mInsertSuccess = false;
                    ((MultiRedditListingFragment) mFragment).stopRefreshProgressbar();
                    Toast.makeText(MultiredditSelectionActivity.this, R.string.error_loading_multi_reddit_list, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    public void getSelectedMultireddit(MultiReddit multiReddit) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_RETURN_MULTIREDDIT, multiReddit);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
        outState.putBoolean(INSERT_SUBSCRIBED_MULTIREDDIT_STATE, mInsertSuccess);
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

    @Override
    public void onLongPress() {
        if (mFragment != null) {
            ((MultiRedditListingFragment) mFragment).goBackToTop();
        }
    }
}