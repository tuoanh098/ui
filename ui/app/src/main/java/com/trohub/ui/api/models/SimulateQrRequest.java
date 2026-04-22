package com.trohub.ui.api.models;

public class SimulateQrRequest {
    private String qrCode;
    private String externalTxnId;
    private Double paidAmount;

    public SimulateQrRequest() {}

    public SimulateQrRequest(String qrCode, String externalTxnId, Double paidAmount) {
        this.qrCode = qrCode;
        this.externalTxnId = externalTxnId;
        this.paidAmount = paidAmount;
    }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getExternalTxnId() { return externalTxnId; }
    public void setExternalTxnId(String externalTxnId) { this.externalTxnId = externalTxnId; }
    public Double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }
}

