package ml.docilealligator.infinityforreddit.activities;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.AppBarStateChangeListener;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeSettingsItem;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.customviews.slidr.widget.SliderPanel;
import ml.docilealligator.infinityforreddit.databinding.ActivityThemePreviewBinding;
import ml.docilealligator.infinityforreddit.font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.font.FontStyle;
import ml.docilealligator.infinityforreddit.font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.fragments.ThemePreviewCommentsFragment;
import ml.docilealligator.infinityforreddit.fragments.ThemePreviewPostsFragment;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class CustomThemePreviewActivity extends AppCompatActivity {
    public static final String EXTRA_CUSTOM_THEME_SETTINGS_ITEMS = "ECTSI";

    private ActivityThemePreviewBinding binding;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    private ArrayList<CustomThemeSettingsItem> customThemeSettingsItems;
    private CustomTheme customTheme;
    private int expandedTabTextColor;
    private int expandedTabBackgroundColor;
    private int expandedTabIndicatorColor;
    private int collapsedTabTextColor;
    private int collapsedTabBackgroundColor;
    private int collapsedTabIndicatorColor;
    private int unsubscribedColor;
    private int subscribedColor;
    private int systemVisibilityToolbarExpanded = 0;
    private int systemVisibilityToolbarCollapsed = 0;
    private SliderPanel mSliderPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        customThemeSettingsItems = getIntent().getParcelableArrayListExtra(EXTRA_CUSTOM_THEME_SETTINGS_ITEMS);
        customTheme = CustomTheme.convertSettingsItemsToCustomTheme(customThemeSettingsItems, "ThemePreview");

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        int systemThemeType = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
        switch (systemThemeType) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                getTheme().applyStyle(R.style.Theme_Normal, true);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                if (mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                    getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                } else {
                    getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                }
                break;
            case 2:
                if (systemDefault) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                }
                if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                    getTheme().applyStyle(R.style.Theme_Normal, true);
                } else {
                    if (mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                        getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                    } else {
                        getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                    }
                }
        }

        boolean immersiveInterface = mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true);
        boolean changeStatusBarIconColor = false;
        if (immersiveInterface) {
            changeStatusBarIconColor = customTheme.isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface;
        }
        boolean isLightStatusbar = customTheme.isLightStatusBar;
        Window window = getWindow();
        View decorView = window.getDecorView();
        boolean isLightNavBar = customTheme.isLightNavBar;
        if (isLightStatusbar) {
            if (isLightNavBar) {
                systemVisibilityToolbarExpanded = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                if (changeStatusBarIconColor) {
                    systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else {
                    systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            } else {
                systemVisibilityToolbarExpanded = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (!changeStatusBarIconColor) {
                    systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
            }
        } else {
            if (isLightNavBar) {
                systemVisibilityToolbarExpanded = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                if (changeStatusBarIconColor) {
                    systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            } else {
                if (changeStatusBarIconColor) {
                    systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
            }
        }
        decorView.setSystemUiVisibility(systemVisibilityToolbarExpanded);
        window.setNavigationBarColor(customTheme.navBarColor);

        getTheme().applyStyle(FontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        binding = ActivityThemePreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
        }

        if (immersiveInterface) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
            } else {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
            adjustToolbar(binding.toolbar);

            Resources resources = getResources();
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                int navBarHeight = resources.getDimensionPixelSize(navBarResourceId);
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) binding.fab.getLayoutParams();
                    params.bottomMargin = navBarHeight;
                    binding.fab.setLayoutParams(params);
                    binding.linearLayoutBottomAppBar.setPadding(0,
                            (int) (6 * getResources().getDisplayMetrics().density), 0, navBarHeight);
                }
            }
        }

        if (changeStatusBarIconColor) {
            binding.appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        decorView.setSystemUiVisibility(systemVisibilityToolbarCollapsed);
                        binding.tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                        binding.tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                        binding.tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                    } else if (state == State.EXPANDED) {
                        decorView.setSystemUiVisibility(systemVisibilityToolbarExpanded);
                        binding.tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                        binding.tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                        binding.tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                    }
                }
            });
        } else {
            binding.appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        binding.tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                        binding.tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                        binding.tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                    } else if (state == State.EXPANDED) {
                        binding.tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                        binding.tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                        binding.tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                    }
                }
            });
        }

        setSupportActionBar(binding.toolbar);

        binding.subscribeSubredditChip.setOnClickListener(view -> {
            if (binding.subscribeSubredditChip.getText().equals(getResources().getString(R.string.subscribe))) {
                binding.subscribeSubredditChip.setText(R.string.unsubscribe);
                binding.subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(subscribedColor));
            } else {
                binding.subscribeSubredditChip.setText(R.string.subscribe);
                binding.subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(unsubscribedColor));
            }
        });

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(sectionsPagerAdapter);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                } else {
                    lockSwipeRightToGoBack();
                }
            }
        });
        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }

    private void applyCustomTheme() {
        binding.coordinatorLayout.setBackgroundColor(customTheme.backgroundColor);
        binding.appBarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.appBarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                binding.collapsingToolbarLayout.setScrimVisibleHeightTrigger(binding.toolbar.getHeight() + binding.tabLayout.getHeight() + getStatusBarHeight() * 2);
            }
        });
        binding.collapsingToolbarLayout.setContentScrimColor(customTheme.colorPrimary);
        binding.subscribeSubredditChip.setTextColor(customTheme.chipTextColor);
        binding.subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(customTheme.unsubscribed));
        applyAppBarLayoutAndToolbarTheme(binding.appBarLayout, binding.toolbar);
        expandedTabTextColor = customTheme.tabLayoutWithExpandedCollapsingToolbarTextColor;
        expandedTabIndicatorColor = customTheme.tabLayoutWithExpandedCollapsingToolbarTabIndicator;
        expandedTabBackgroundColor = customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground;
        collapsedTabTextColor = customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor;
        collapsedTabIndicatorColor = customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator;
        collapsedTabBackgroundColor = customTheme.tabLayoutWithCollapsedCollapsingToolbarTabBackground;
        binding.toolbarLinearLayout.setBackgroundColor(customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground);
        binding.extraPaddingView.setBackgroundColor(customTheme.colorPrimary);
        binding.subredditNameTextView.setTextColor(customTheme.subreddit);
        binding.userNameTextView.setTextColor(customTheme.username);
        binding.subscribeSubredditChip.setTextColor(customTheme.chipTextColor);
        binding.primaryTextTextView.setTextColor(customTheme.primaryTextColor);
        binding.secondaryTextTextView.setTextColor(customTheme.secondaryTextColor);
        binding.bottomNavigation.setBackgroundTint(ColorStateList.valueOf(customTheme.bottomAppBarBackgroundColor));
        int bottomAppBarIconColor = customTheme.bottomAppBarIconColor;
        binding.subscriptionsBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.multiRedditBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.messageBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.profileBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        applyTabLayoutTheme(binding.tabLayout);
        applyFABTheme(binding.fab);
        unsubscribedColor = customTheme.unsubscribed;
        subscribedColor = customTheme.subscribed;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected void applyAppBarLayoutAndToolbarTheme(AppBarLayout appBarLayout, Toolbar toolbar) {
        appBarLayout.setBackgroundColor(customTheme.colorPrimary);
        toolbar.setTitleTextColor(customTheme.toolbarPrimaryTextAndIconColor);
        toolbar.setSubtitleTextColor(customTheme.toolbarSecondaryTextColor);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(customTheme.toolbarPrimaryTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (toolbar.getOverflowIcon() != null) {
            toolbar.getOverflowIcon().setColorFilter(customTheme.toolbarPrimaryTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void adjustToolbar(Toolbar toolbar) {
        int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarResourceId > 0) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            int statusBarHeight = getResources().getDimensionPixelSize(statusBarResourceId);
            params.topMargin = statusBarHeight;
            toolbar.setLayoutParams(params);
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                ((ViewGroup.MarginLayoutParams) binding.toolbarLinearLayout.getLayoutParams()).setMargins(0,
                        TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()) + statusBarHeight, 0, 0);
            }
        }
    }

    protected void applyTabLayoutTheme(TabLayout tabLayout) {
        int toolbarAndTabBackgroundColor = customTheme.colorPrimary;
        tabLayout.setBackgroundColor(toolbarAndTabBackgroundColor);
        tabLayout.setSelectedTabIndicatorColor(customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator);
        tabLayout.setTabTextColors(customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor,
                customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor);
    }

    protected void applyFABTheme(FloatingActionButton fab) {
        fab.setBackgroundTintList(ColorStateList.valueOf(customTheme.colorAccent));
        fab.setImageTintList(ColorStateList.valueOf(customTheme.fabIconColor));
    }

    public CustomTheme getCustomTheme() {
        return customTheme;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    private void lockSwipeRightToGoBack() {
        if (mSliderPanel != null) {
            mSliderPanel.lock();
        }
    }

    private void unlockSwipeRightToGoBack() {
        if (mSliderPanel != null) {
            mSliderPanel.unlock();
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ThemePreviewPostsFragment themePreviewPostsFragment;
        private ThemePreviewCommentsFragment themePreviewCommentsFragment;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ThemePreviewPostsFragment();
            }
            return new ThemePreviewCommentsFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Posts";
                case 1:
                    return "Comments";
            }
            return null;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    themePreviewPostsFragment = (ThemePreviewPostsFragment) fragment;
                    break;
                case 1:
                    themePreviewCommentsFragment = (ThemePreviewCommentsFragment) fragment;
            }
            return fragment;
        }
    }
}
