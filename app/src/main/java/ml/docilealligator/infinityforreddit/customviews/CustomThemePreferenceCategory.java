package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import ml.docilealligator.infinityforreddit.CustomThemeWrapperReceiver;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;

public class CustomThemePreferenceCategory extends PreferenceCategory implements CustomThemeWrapperReceiver {
    private CustomThemeWrapper customThemeWrapper;

    public CustomThemePreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomThemePreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomThemePreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomThemePreferenceCategory(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View titleTextView = holder.findViewById(android.R.id.title);

        if (customThemeWrapper != null) {
            if (titleTextView instanceof TextView) {
                ((TextView) titleTextView).setTextColor(customThemeWrapper.getColorAccent());
            }
        }
    }

    @Override
    public void setCustomThemeWrapper(CustomThemeWrapper customThemeWrapper) {
        this.customThemeWrapper = customThemeWrapper;
    }
}
