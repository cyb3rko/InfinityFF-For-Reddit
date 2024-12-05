package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.databinding.FragmentNsfwAndSpoilerBinding;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class NsfwAndSpoilerFragment extends Fragment {
    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    private FragmentNsfwAndSpoilerBinding binding;

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences nsfwAndBlurringSharedPreferences;

    private SettingsActivity activity;
    private boolean blurNsfw;
    private boolean doNotBlurNsfwInNsfwSubreddits;
    private boolean disableNsfwForever;
    private boolean manuallyCheckDisableNsfwForever = true;

    public NsfwAndSpoilerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNsfwAndSpoilerBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        applyCustomTheme();

        rootView.setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        boolean enableNsfw = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);
        blurNsfw = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
        doNotBlurNsfwInNsfwSubreddits = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, false);
        boolean blurSpoiler = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);
        disableNsfwForever = sharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false);

        if (enableNsfw) {
            binding.blurNsfwLinearLayout.setVisibility(View.VISIBLE);
            binding.doNotBlurNsfwInNsfwSubredditsLinearLayout.setVisibility(View.VISIBLE);
        }

        binding.enableNsfwSwitch.setChecked(enableNsfw);
        binding.blurNsfwSwitch.setChecked(blurNsfw);
        binding.doNotBlurNsfwInNsfwSubredditsSwitch.setChecked(doNotBlurNsfwInNsfwSubreddits);
        binding.blurSpoilerSwitch.setChecked(blurSpoiler);
        binding.disableNsfwForeverSwitch.setChecked(disableNsfwForever);
        binding.disableNsfwForeverSwitch.setEnabled(!disableNsfwForever);
        if (disableNsfwForever) {
            binding.disableNsfwForeverTextView.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
            binding.disableNsfwForeverLinearLayout.setEnabled(false);
        }

        binding.enableNsfwLinearLayout.setOnClickListener(view -> binding.enableNsfwSwitch.performClick());
        binding.enableNsfwSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, b).apply();
            if (b) {
                binding.blurNsfwLinearLayout.setVisibility(View.VISIBLE);
                binding.doNotBlurNsfwInNsfwSubredditsLinearLayout.setVisibility(View.VISIBLE);
            } else {
                binding.blurNsfwLinearLayout.setVisibility(View.GONE);
                binding.doNotBlurNsfwInNsfwSubredditsLinearLayout.setVisibility(View.GONE);
            }
            EventBus.getDefault().post(new ChangeNSFWEvent(b));
        });

        binding.blurNsfwLinearLayout.setOnClickListener(view -> binding.blurNsfwSwitch.performClick());
        binding.blurNsfwSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, b).apply();
            EventBus.getDefault().post(new ChangeNSFWBlurEvent(b, doNotBlurNsfwInNsfwSubreddits));
        });

        binding.doNotBlurNsfwInNsfwSubredditsLinearLayout.setOnClickListener(view -> {
            binding.doNotBlurNsfwInNsfwSubredditsSwitch.performClick();
        });
        binding.doNotBlurNsfwInNsfwSubredditsSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, b).apply();
            EventBus.getDefault().post(new ChangeNSFWBlurEvent(blurNsfw, b));
        });

        binding.blurSpoilerLinearLayout.setOnClickListener(view -> binding.blurSpoilerSwitch.performClick());
        binding.blurSpoilerSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, b).apply();
            EventBus.getDefault().post(new ChangeSpoilerBlurEvent(b));
        });

        binding.disableNsfwForeverLinearLayout.setOnClickListener(view -> {
            binding.disableNsfwForeverSwitch.performClick();
        });
        binding.disableNsfwForeverSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (manuallyCheckDisableNsfwForever) {
                manuallyCheckDisableNsfwForever = false;
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.warning)
                        .setMessage(R.string.disable_nsfw_forever_message)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> {
                            sharedPreferences.edit().putBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, true).apply();
                            disableNsfwForever = true;
                            binding.disableNsfwForeverSwitch.setEnabled(false);
                            binding.disableNsfwForeverLinearLayout.setEnabled(false);
                            binding.disableNsfwForeverSwitch.setChecked(true);
                            binding.disableNsfwForeverTextView.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
                            EventBus.getDefault().post(new ChangeNSFWEvent(false));
                        })
                        .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                            binding.disableNsfwForeverSwitch.setChecked(false);
                            manuallyCheckDisableNsfwForever = true;
                        })
                        .setOnDismissListener(dialogInterface -> {
                            if (!disableNsfwForever) {
                                binding.disableNsfwForeverSwitch.setChecked(false);
                            }
                            manuallyCheckDisableNsfwForever = true;
                        })
                        .show();
            }
        });
        return rootView;
    }

    private void applyCustomTheme() {
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        binding.enableNsfwTextView.setCompoundDrawablesWithIntrinsicBounds(Utils.getTintedDrawable(activity, R.drawable.ic_nsfw_on_24dp, activity.customThemeWrapper.getPrimaryIconColor()), null, null, null);
        binding.enableNsfwTextView.setTextColor(primaryTextColor);
        binding.blurNsfwTextView.setTextColor(primaryTextColor);
        binding.doNotBlurNsfwTextView.setTextColor(primaryTextColor);
        binding.blurSpoilerTextView.setTextColor(primaryTextColor);
        binding.dangerousTextView.setTextColor(primaryTextColor);
        binding.disableNsfwForeverTextView.setTextColor(primaryTextColor);
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