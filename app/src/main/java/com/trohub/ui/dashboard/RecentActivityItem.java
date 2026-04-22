package com.trohub.ui.dashboard;

public class RecentActivityItem {
    private final String title;
    private final String subtitle;
    private final String timeLabel;

    public RecentActivityItem(String title, String subtitle, String timeLabel) {
        this.title = title;
        this.subtitle = subtitle;
        this.timeLabel = timeLabel;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTimeLabel() {
        return timeLabel;
    }
}
