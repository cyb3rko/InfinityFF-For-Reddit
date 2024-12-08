package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import ml.docilealligator.infinityforreddit.CustomThemeWrapperReceiver;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;

public class CustomThemePreference extends Preference implements CustomThemeWrapperReceiver {
    private CustomThemeWrapper customThemeWrapper;

    public CustomThemePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomThemePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomThemePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomThemePreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View iconImageView = holder.findViewById(android.R.id.icon);
        View titleTextView = holder.findViewById(android.R.id.title);
        View summaryTextView = holder.findViewById(android.R.id.summary);

        if (customThemeWrapper != null) {
            if (iconImageView instanceof ImageView) {
                if (isEnabled()) {
                    ((ImageView) iconImageView).setColorFilter(customThemeWrapper.getPrimaryIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    ((ImageView) iconImageView).setColorFilter(customThemeWrapper.getSecondaryTextColor(), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
            if (titleTextView instanceof TextView) {
                ((TextView) titleTextView).setTextColor(customThemeWrapper.getPrimaryTextColor());
            }
            if (summaryTextView instanceof TextView) {
                ((TextView) summaryTextView).setTextColor(customThemeWrapper.getSecondaryTextColor());
            }
        }
    }

    @Override
    public void setCustomThemeWrapper(CustomThemeWrapper customThemeWrapper) {
        this.customThemeWrapper = customThemeWrapper;
    }
}
