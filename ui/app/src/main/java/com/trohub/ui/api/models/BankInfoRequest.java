package com.trohub.ui.api.models;

public class BankInfoRequest {
    private String accountNumber;
    private String ownerName;
    private String bankName;
    private String imageBase64;

    public BankInfoRequest(String accountNumber, String ownerName, String bankName, String imageBase64) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.bankName = bankName;
        this.imageBase64 = imageBase64;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
