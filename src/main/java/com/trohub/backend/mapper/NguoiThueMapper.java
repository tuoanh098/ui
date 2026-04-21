package com.trohub.backend.mapper;

import com.trohub.backend.dto.NguoiThueDto;
import com.trohub.backend.modal.NguoiThue;

public class NguoiThueMapper {

    public static NguoiThueDto toDto(NguoiThue e) {
        if (e == null) return null;
        return NguoiThueDto.builder()
                .id(e.getId())
                .cccd(e.getCccd())
                .hoTen(e.getHoTen())
                .ngaySinh(e.getNgaySinh())
                .gioiTinh(e.getGioiTinh())
                .diaChi(e.getDiaChi())
                .sdt(e.getSdt())
                .queQuan(e.getQueQuan())
                .ngheNghiep(e.getNgheNghiep())
                .thongTinLienLac(e.getThongTinLienLac())
                .taiKhoanId(e.getTaiKhoan() != null ? e.getTaiKhoan().getId() : null)
                .sophong(e.getSophong())
                .build();
    }

    public static NguoiThue toEntity(NguoiThueDto dto) {
        if (dto == null) return null;
        return NguoiThue.builder()
                .id(dto.getId())
                .cccd(dto.getCccd())
                .hoTen(dto.getHoTen())
                .ngaySinh(dto.getNgaySinh())
                .gioiTinh(dto.getGioiTinh())
                .diaChi(dto.getDiaChi())
                .sdt(dto.getSdt())
                .queQuan(dto.getQueQuan())
                .ngheNghiep(dto.getNgheNghiep())
                .thongTinLienLac(dto.getThongTinLienLac())
                // Note: taiKhoan association should be resolved in service (fetch by id)
                .sophong(dto.getSophong())
                .build();
    }
}

