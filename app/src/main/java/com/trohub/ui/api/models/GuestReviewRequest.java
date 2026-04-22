package com.trohub.ui.api.models;

public class GuestReviewRequest {
    private String note;

    public GuestReviewRequest() {
    }

    public GuestReviewRequest(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
