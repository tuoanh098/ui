package com.trohub.ui.api.models;

import java.util.List;

public class Incident {
    private Long id;
    private String loai;
    private String moTa;
    private Long toaNhaId;
    private Long phongId;
    private Long reportedBy;
    private String reportedAt;
    private String resolvedAt;
    private String status;
    private List<String> imagePaths;

    public Incident() {}

    public Incident(String loai, String moTa, Long toaNhaId, Long phongId, Long reportedBy, String status) {
        this.loai = loai;
        this.moTa = moTa;
        this.toaNhaId = toaNhaId;
        this.phongId = phongId;
        this.reportedBy = reportedBy;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Long getToaNhaId() { return toaNhaId; }
    public void setToaNhaId(Long toaNhaId) { this.toaNhaId = toaNhaId; }
    public Long getPhongId() { return phongId; }
    public void setPhongId(Long phongId) { this.phongId = phongId; }
    public Long getReportedBy() { return reportedBy; }
    public void setReportedBy(Long reportedBy) { this.reportedBy = reportedBy; }
    public String getReportedAt() { return reportedAt; }
    public void setReportedAt(String reportedAt) { this.reportedAt = reportedAt; }
    public String getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(String resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }
}

