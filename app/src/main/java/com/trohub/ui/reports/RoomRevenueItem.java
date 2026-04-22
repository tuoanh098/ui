package com.trohub.ui.reports;

public class RoomRevenueItem {
    private final Long roomId;
    private final String roomCode;
    private final String buildingName;
    private final double revenue;
    private final double expectedAmount;
    private final int invoiceCount;
    private final int paidInvoices;
    private final int unpaidInvoices;
    private final int draftInvoices;
    private final int partialInvoices;
    private final int overdueInvoices;

    public RoomRevenueItem(
            Long roomId,
            String roomCode,
            String buildingName,
            double revenue,
            double expectedAmount,
            int invoiceCount,
            int paidInvoices,
            int unpaidInvoices,
            int draftInvoices,
            int partialInvoices,
            int overdueInvoices
    ) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.buildingName = buildingName;
        this.revenue = revenue;
        this.expectedAmount = expectedAmount;
        this.invoiceCount = invoiceCount;
        this.paidInvoices = paidInvoices;
        this.unpaidInvoices = unpaidInvoices;
        this.draftInvoices = draftInvoices;
        this.partialInvoices = partialInvoices;
        this.overdueInvoices = overdueInvoices;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public double getRevenue() {
        return revenue;
    }

    public double getExpectedAmount() {
        return expectedAmount;
    }

    public int getInvoiceCount() {
        return invoiceCount;
    }

    public int getPaidInvoices() {
        return paidInvoices;
    }

    public int getUnpaidInvoices() {
        return unpaidInvoices;
    }

    public int getDraftInvoices() {
        return draftInvoices;
    }

    public int getPartialInvoices() {
        return partialInvoices;
    }

    public int getOverdueInvoices() {
        return overdueInvoices;
    }
}
