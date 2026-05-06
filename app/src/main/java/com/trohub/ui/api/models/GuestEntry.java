package com.trohub.ui.api.models;

import java.util.List;

public class GuestEntry {
    private Long id;
    private String ten;
    private String cmnd;
    private String sdt;
    private Long phongId;
    private String loai;
    private String timestamp;
    private String ghiChu;
    private String approvalStatus;
    private List<String> imagePaths;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getCmnd() { return cmnd; }
    public void setCmnd(String cmnd) { this.cmnd = cmnd; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public Long getPhongId() { return phongId; }
    public void setPhongId(Long phongId) { this.phongId = phongId; }
    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }
}
