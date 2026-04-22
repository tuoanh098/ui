package com.trohub.ui.api.models;

public class QrResponse {
    private String qrCode;
    private String qrPayload;
    private String qrImageDataUrl;
    private Double expectedAmount;
    private String expiresAt;
    private String invoiceNumber;
    private String bankImageUrl;

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
    public String getQrImageDataUrl() { return qrImageDataUrl; }
    public void setQrImageDataUrl(String qrImageDataUrl) { this.qrImageDataUrl = qrImageDataUrl; }
    public Double getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(Double expectedAmount) { this.expectedAmount = expectedAmount; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getBankImageUrl() { return bankImageUrl; }
    public void setBankImageUrl(String bankImageUrl) { this.bankImageUrl = bankImageUrl; }
}

