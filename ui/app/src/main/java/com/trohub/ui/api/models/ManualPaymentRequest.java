package com.trohub.ui.api.models;

public class ManualPaymentRequest {
    private Double amount;
    private String paymentMethod;
    private String transactionId;
    private String paymentDate;

    public ManualPaymentRequest() {}

    public ManualPaymentRequest(Double amount, String paymentMethod, String transactionId, String paymentDate) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.paymentDate = paymentDate;
    }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
}

