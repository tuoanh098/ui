package com.trohub.ui.api.models;

public class Tenant {
    private Long id;
    private String cccd;
    private String hoTen;
    private String ngaySinh;
    private String gioiTinh;
    private String sdt;
    private String diaChi;
    private String queQuan;
    private String ngheNghiep;
    private String thongTinLienLac;
    private Long taiKhoanId;
    private Long sophong;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getQueQuan() { return queQuan; }
    public void setQueQuan(String queQuan) { this.queQuan = queQuan; }

    public String getNgheNghiep() { return ngheNghiep; }
    public void setNgheNghiep(String ngheNghiep) { this.ngheNghiep = ngheNghiep; }

    public String getThongTinLienLac() { return thongTinLienLac; }
    public void setThongTinLienLac(String thongTinLienLac) { this.thongTinLienLac = thongTinLienLac; }

    public Long getTaiKhoanId() { return taiKhoanId; }
    public void setTaiKhoanId(Long taiKhoanId) { this.taiKhoanId = taiKhoanId; }

    public Long getSophong() { return sophong; }
    public void setSophong(Long sophong) { this.sophong = sophong; }
}

