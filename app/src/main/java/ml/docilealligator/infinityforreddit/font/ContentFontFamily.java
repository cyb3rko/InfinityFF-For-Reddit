package ml.docilealligator.infinityforreddit.font;

import ml.docilealligator.infinityforreddit.R;

public enum ContentFontFamily {
    Default(R.style.ContentFontFamily, "Default");

    private final int resId;
    private final String title;

    ContentFontFamily(int resId, String title) {
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
