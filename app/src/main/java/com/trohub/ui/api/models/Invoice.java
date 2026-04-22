package com.trohub.ui.api.models;

public class Invoice {
    private Long id;
    private String invoiceNumber;
    private Long tenantId;
    private String tenantName;
    private String tenantPhone;
    private Long roomId;
    private String roomCode;
    private Long buildingId;
    private String buildingName;
    private Long landlordId;
    private String landlordName;
    private Integer roomShareCount;
    private Double roomTotalAmount;
    private Integer periodMonth;
    private Integer periodYear;
    private String issueDate;
    private String dueDate;
    private Double totalAmount;
    private Double penaltyAmount;
    private String status;
    private java.util.List<PaymentDto> payments;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public String getTenantPhone() { return tenantPhone; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public Long getLandlordId() { return landlordId; }
    public void setLandlordId(Long landlordId) { this.landlordId = landlordId; }
    public String getLandlordName() { return landlordName; }
    public void setLandlordName(String landlordName) { this.landlordName = landlordName; }
    public Integer getRoomShareCount() { return roomShareCount; }
    public void setRoomShareCount(Integer roomShareCount) { this.roomShareCount = roomShareCount; }
    public Double getRoomTotalAmount() { return roomTotalAmount; }
    public void setRoomTotalAmount(Double roomTotalAmount) { this.roomTotalAmount = roomTotalAmount; }
    public Integer getPeriodMonth() { return periodMonth; }
    public void setPeriodMonth(Integer periodMonth) { this.periodMonth = periodMonth; }
    public Integer getPeriodYear() { return periodYear; }
    public void setPeriodYear(Integer periodYear) { this.periodYear = periodYear; }
    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Double getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(Double penaltyAmount) { this.penaltyAmount = penaltyAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.util.List<PaymentDto> getPayments() { return payments; }
    public void setPayments(java.util.List<PaymentDto> payments) { this.payments = payments; }
}

