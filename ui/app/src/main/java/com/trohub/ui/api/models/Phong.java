package com.trohub.ui.api.models;

public class Phong {
    private Long id;
    private String maPhong;
    private Long toaNhaId;
    private Integer soGiuong;
    private String trangThai;
    private String moTa;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public Long getToaNhaId() { return toaNhaId; }
    public void setToaNhaId(Long toaNhaId) { this.toaNhaId = toaNhaId; }

    public Integer getSoGiuong() { return soGiuong; }
    public void setSoGiuong(Integer soGiuong) { this.soGiuong = soGiuong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}
