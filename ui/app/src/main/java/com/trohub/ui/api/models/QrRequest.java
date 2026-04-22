package com.trohub.ui.api.models;

public class QrRequest {
    private Long hoaDonId;
    private Double amount;

    public QrRequest(Long hoaDonId) {
        this.hoaDonId = hoaDonId;
    }

    public QrRequest(Long hoaDonId, Double amount) {
        this.hoaDonId = hoaDonId;
        this.amount = amount;
    }

    public Long getHoaDonId() { return hoaDonId; }
    public void setHoaDonId(Long hoaDonId) { this.hoaDonId = hoaDonId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}

