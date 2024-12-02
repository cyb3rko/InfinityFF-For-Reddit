package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeSettingsItem;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.ColorPickerDialog;
import ml.docilealligator.infinityforreddit.databinding.ItemCustomThemeColorItemBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemCustomThemeSwitchItemBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemThemeNameBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomizeThemeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_COLOR = 1;
    private static final int VIEW_TYPE_SWITCH = 2;
    private static final int VIEW_TYPE_THEME_NAME = 3;
    private BaseActivity activity;
    private CustomThemeWrapper customThemeWrapper;
    private ArrayList<CustomThemeSettingsItem> customThemeSettingsItems;
    private String themeName;

    public CustomizeThemeRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                             String themeName) {
        this.activity = activity;
        this.customThemeWrapper = customThemeWrapper;
        customThemeSettingsItems = new ArrayList<>();
        this.themeName = themeName;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_THEME_NAME;
        } else if (position > 3 && position < customThemeSettingsItems.size() - 2) {
            return VIEW_TYPE_COLOR;
        }

        return VIEW_TYPE_SWITCH;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SWITCH) {
            return new ThemeSwitchItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_theme_switch_item, parent, false));
        } else if (viewType == VIEW_TYPE_THEME_NAME) {
            return new ThemeNameItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_name, parent, false));
        }

        return new ThemeColorItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_theme_color_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ThemeColorItemViewHolder) {
            CustomThemeSettingsItem customThemeSettingsItem = customThemeSettingsItems.get(position - 1);
            ((ThemeColorItemViewHolder) holder).binding.themeItemNameTextView.setText(customThemeSettingsItem.itemName);
            ((ThemeColorItemViewHolder) holder).binding.themeItemInfoTextView.setText(customThemeSettingsItem.itemDetails);
            ((ThemeColorItemViewHolder) holder).binding.colorImageView.setBackgroundTintList(ColorStateList.valueOf(customThemeSettingsItem.colorValue));
            holder.itemView.setOnClickListener(view -> {
                new ColorPickerDialog(activity, customThemeSettingsItem.colorValue, color -> {
                    customThemeSettingsItem.colorValue = color;
                    ((ThemeColorItemViewHolder) holder).binding.colorImageView.setBackgroundTintList(ColorStateList.valueOf(color));
                }).show();
            });
        } else if (holder instanceof ThemeSwitchItemViewHolder) {
            CustomThemeSettingsItem customThemeSettingsItem = customThemeSettingsItems.get(position - 1);
            ((ThemeSwitchItemViewHolder) holder).binding.themeItemNameTextView.setText(customThemeSettingsItem.itemName);
            ((ThemeSwitchItemViewHolder) holder).binding.themeItemInfoTextView.setText(customThemeSettingsItem.itemName);
            ((ThemeSwitchItemViewHolder) holder).binding.themeItemSwitch.setChecked(customThemeSettingsItem.isEnabled);
            ((ThemeSwitchItemViewHolder) holder).binding.themeItemSwitch.setOnClickListener(view -> customThemeSettingsItem.isEnabled = ((ThemeSwitchItemViewHolder) holder).binding.themeItemSwitch.isChecked());
            holder.itemView.setOnClickListener(view -> ((ThemeSwitchItemViewHolder) holder).binding.themeItemSwitch.performClick());
        } else if (holder instanceof ThemeNameItemViewHolder) {
            ((ThemeNameItemViewHolder) holder).binding.themeNameTextView.setText(themeName);
            holder.itemView.setOnClickListener(view -> {
                View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
                EditText themeNameEditText = dialogView.findViewById(R.id.name_edit_text_edit_name_dialog);
                themeNameEditText.setText(themeName);
                themeNameEditText.requestFocus();
                Utils.showKeyboard(activity, new Handler(), themeNameEditText);
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.edit_theme_name)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, (dialogInterface, i)
                                -> {
                            Utils.hideKeyboard(activity);
                            themeName = themeNameEditText.getText().toString();
                            ((ThemeNameItemViewHolder) holder).binding.themeNameTextView.setText(themeName);
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            Utils.hideKeyboard(activity);
                        })
                        .setOnDismissListener(dialogInterface -> {
                            Utils.hideKeyboard(activity);
                        })
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return customThemeSettingsItems.size() + 1;
    }

    public void setCustomThemeSettingsItem(ArrayList<CustomThemeSettingsItem> customThemeSettingsItems) {
        this.customThemeSettingsItems.clear();
        this.customThemeSettingsItems.addAll(customThemeSettingsItems);
        notifyDataSetChanged();
    }

    public String getThemeName() {
        return themeName;
    }

    class ThemeColorItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemCustomThemeColorItemBinding binding;

        ThemeColorItemViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCustomThemeColorItemBinding.bind(itemView);

            binding.themeItemNameTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());
            binding.themeItemInfoTextView.setTextColor(customThemeWrapper.getSecondaryTextColor());

            if (activity.typeface != null) {
                binding.themeItemNameTextView.setTypeface(activity.typeface);
                binding.themeItemInfoTextView.setTypeface(activity.typeface);
            }
        }
    }

    class ThemeSwitchItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemCustomThemeSwitchItemBinding binding;

        ThemeSwitchItemViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCustomThemeSwitchItemBinding.bind(itemView);

            binding.themeItemNameTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());
            binding.themeItemInfoTextView.setTextColor(customThemeWrapper.getSecondaryTextColor());

            if (activity.typeface != null) {
                binding.themeItemNameTextView.setTypeface(activity.typeface);
                binding.themeItemInfoTextView.setTypeface(activity.typeface);
            }
        }
    }

    class ThemeNameItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemThemeNameBinding binding;

        public ThemeNameItemViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemThemeNameBinding.bind(itemView);

            binding.themeNameTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());
            binding.descriptionTextView.setTextColor(customThemeWrapper.getSecondaryTextColor());

            if (activity.typeface != null) {
                binding.themeNameTextView.setTypeface(activity.typeface);
                binding.descriptionTextView.setTypeface(activity.typeface);
            }
        }
    }
}
