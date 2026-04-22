package com.trohub.ui.api.models;

public class Contract {
    private Long id;
    private String maHopDong;
    private Long phongId;
    private Long nguoiId;
    private String ngayBatDau;
    private String ngayKetThuc;
    private Double tienThue;
    private Double tienDienPerUnit;
    private Double tienNuocFixed;
    private String trangThai;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaHopDong() { return maHopDong; }
    public void setMaHopDong(String maHopDong) { this.maHopDong = maHopDong; }
    public Long getPhongId() { return phongId; }
    public void setPhongId(Long phongId) { this.phongId = phongId; }
    public Long getNguoiId() { return nguoiId; }
    public void setNguoiId(Long nguoiId) { this.nguoiId = nguoiId; }
    public String getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(String ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public String getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(String ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    public Double getTienThue() { return tienThue; }
    public void setTienThue(Double tienThue) { this.tienThue = tienThue; }
    public Double getTienDienPerUnit() { return tienDienPerUnit; }
    public void setTienDienPerUnit(Double tienDienPerUnit) { this.tienDienPerUnit = tienDienPerUnit; }
    public Double getTienNuocFixed() { return tienNuocFixed; }
    public void setTienNuocFixed(Double tienNuocFixed) { this.tienNuocFixed = tienNuocFixed; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

