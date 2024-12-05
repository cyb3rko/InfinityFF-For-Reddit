package ml.docilealligator.infinityforreddit.font;

import ml.docilealligator.infinityforreddit.R;

public enum TitleFontStyle {
    Normal(R.style.TitleFontStyle_Normal, "Normal");

    private final int resId;
    private final String title;

    TitleFontStyle(int resId, String title) {
        this.resId = resId;
        this.title = title;
    }

    public int getResId() {
        return resId;
    }

    public String getTitle() {
        return title;
    }
}
