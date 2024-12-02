package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostHistoryBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class PostHistoryFragment extends Fragment {
    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    private FragmentPostHistoryBinding binding;

    @Inject
    @Named("post_history")
    SharedPreferences postHistorySharedPreferences;
    private SettingsActivity activity;

    public PostHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostHistoryBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        rootView.setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());
        applyCustomTheme();

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, activity.typeface);
        }

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);
        if (accountName == null) {
            binding.infoTextView.setText(R.string.only_for_logged_in_user);
            binding.markPostsAsReadLinearLayout.setVisibility(View.GONE);
            binding.markPostsAsReadAfterVotingLinearLayout.setVisibility(View.GONE);
            binding.markPostsAsReadOnScrollLinearLayout.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyLinearLayout.setVisibility(View.GONE);
            return rootView;
        }

        binding.markPostsAsReadSwitch.setChecked(postHistorySharedPreferences.getBoolean(
                accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, false));
        binding.markPostsAsReadAfterVotingSwitch.setChecked(postHistorySharedPreferences.getBoolean(
                accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_AFTER_VOTING_BASE, false));
        binding.markPostsAsReadOnScrollSwitch.setChecked(postHistorySharedPreferences.getBoolean(
                accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_ON_SCROLL_BASE, false));
        binding.hideReadPostsAutomaticallySwitch.setChecked(postHistorySharedPreferences.getBoolean(
                accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, false));

        binding.markPostsAsReadLinearLayout.setOnClickListener(view -> {
            binding.markPostsAsReadSwitch.performClick();
        });

        binding.markPostsAsReadSwitch.setOnCheckedChangeListener((compoundButton, b) ->
                postHistorySharedPreferences.edit().putBoolean(accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, b).apply());

        binding.markPostsAsReadAfterVotingLinearLayout.setOnClickListener(view -> binding.markPostsAsReadAfterVotingSwitch.performClick());

        binding.markPostsAsReadAfterVotingSwitch.setOnCheckedChangeListener((compoundButton, b) ->
                postHistorySharedPreferences.edit().putBoolean(accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_AFTER_VOTING_BASE, b).apply());

        binding.markPostsAsReadOnScrollLinearLayout.setOnClickListener(view -> binding.markPostsAsReadOnScrollSwitch.performClick());

        binding.markPostsAsReadOnScrollSwitch.setOnCheckedChangeListener((compoundButton, b) -> postHistorySharedPreferences.edit().putBoolean(accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_ON_SCROLL_BASE, b).apply());

        binding.hideReadPostsAutomaticallyLinearLayout.setOnClickListener(view -> binding.hideReadPostsAutomaticallySwitch.performClick());

        binding.hideReadPostsAutomaticallySwitch.setOnCheckedChangeListener((compoundButton, b) -> postHistorySharedPreferences.edit().putBoolean(accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, b).apply());

        return rootView;
    }

    private void applyCustomTheme() {
        binding.infoTextView.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
        Drawable infoDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_info_preference_24dp, activity.customThemeWrapper.getPrimaryIconColor());
        binding.infoTextView.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        binding.markPostsAsReadTextView.setTextColor(primaryTextColor);
        binding.markPostsAsReadAfterVotingTextView.setTextColor(primaryTextColor);
        binding.markPostsAsReadOnScrollTextView.setTextColor(primaryTextColor);
        binding.hideReadPostsAutomaticallyTextView.setTextColor(primaryTextColor);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (SettingsActivity) context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}