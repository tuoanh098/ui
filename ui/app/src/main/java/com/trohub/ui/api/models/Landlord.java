package com.trohub.ui.api.models;

public class Landlord {
    private Long id;
    private String ten;
    private String email;
    private String sdt;
    private String diaChi;
    private Long taiKhoanId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public Long getTaiKhoanId() { return taiKhoanId; }
    public void setTaiKhoanId(Long taiKhoanId) { this.taiKhoanId = taiKhoanId; }
}
