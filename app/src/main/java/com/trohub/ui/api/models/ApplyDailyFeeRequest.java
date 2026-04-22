package com.trohub.ui.api.models;

public class ApplyDailyFeeRequest {
    private Long hoaDonId;
    private int perDayAmount;

    public ApplyDailyFeeRequest(Long hoaDonId, int perDayAmount) {
        this.hoaDonId = hoaDonId;
        this.perDayAmount = perDayAmount;
    }

    public Long getHoaDonId() { return hoaDonId; }
    public void setHoaDonId(Long hoaDonId) { this.hoaDonId = hoaDonId; }
    public int getPerDayAmount() { return perDayAmount; }
    public void setPerDayAmount(int perDayAmount) { this.perDayAmount = perDayAmount; }
}
