package com.iar.myapplication;

public class Alert {
    private final int iconId;
    private final String title;
    private final String subtitle;

    public Alert(int iconId, String title, String subtitle) {
        this.iconId = iconId;
        this.title = title;
        this.subtitle = subtitle;
    }

    public int getIconId() {
        return iconId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
