package ml.docilealligator.infinityforreddit.font;

import ml.docilealligator.infinityforreddit.R;

public enum ContentFontStyle {
    XSmall(R.style.ContentFontStyle_XSmall, "XSmall"),
    Small(R.style.ContentFontStyle_Small, "Small"),
    Normal(R.style.ContentFontStyle_Normal, "Normal");

    private final int resId;
    private final String title;

    ContentFontStyle(int resId, String title) {
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
