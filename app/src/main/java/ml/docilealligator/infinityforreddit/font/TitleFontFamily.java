package ml.docilealligator.infinityforreddit.font;

import ml.docilealligator.infinityforreddit.R;

public enum TitleFontFamily {
    Default(R.style.TitleFontFamily, "Default");

    private final int resId;
    private final String title;

    TitleFontFamily(int resId, String title) {
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
