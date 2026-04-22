package com.trohub.ui.reports;

public class RoomRevenueItem {
    private final Long roomId;
    private final String roomCode;
    private final String buildingName;
    private final double revenue;
    private final int paidInvoices;
    private final int overdueInvoices;

    public RoomRevenueItem(Long roomId, String roomCode, String buildingName, double revenue, int paidInvoices, int overdueInvoices) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.buildingName = buildingName;
        this.revenue = revenue;
        this.paidInvoices = paidInvoices;
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

    public int getPaidInvoices() {
        return paidInvoices;
    }

    public int getOverdueInvoices() {
        return overdueInvoices;
    }
}
