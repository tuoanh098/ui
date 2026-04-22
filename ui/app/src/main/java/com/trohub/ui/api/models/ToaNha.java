package com.trohub.ui.api.models;

public class ToaNha {
    private Long id;
    private String ten;
    private String diaChi;
    private Long chuTroId;
    private Long roomCount;
    private Long occupiedCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public Long getChuTroId() { return chuTroId; }
    public void setChuTroId(Long chuTroId) { this.chuTroId = chuTroId; }
    public Long getRoomCount() { return roomCount; }
    public void setRoomCount(Long roomCount) { this.roomCount = roomCount; }
    public Long getOccupiedCount() { return occupiedCount; }
    public void setOccupiedCount(Long occupiedCount) { this.occupiedCount = occupiedCount; }
}

