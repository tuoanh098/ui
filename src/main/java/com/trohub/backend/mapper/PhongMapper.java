package com.trohub.backend.mapper;

import com.trohub.backend.dto.PhongDto;
import com.trohub.backend.modal.Phong;

public class PhongMapper {

    public static PhongDto toDto(Phong p) {
        if (p == null) return null;
        return PhongDto.builder()
                .id(p.getId())
                .maPhong(p.getMaPhong())
                .toaNhaId(p.getToaNhaId())
                .khuId(p.getKhuId())
                .loaiPhongId(p.getLoaiPhongId())
                .soGiuong(p.getSoGiuong())
                .trangThai(p.getTrangThai())
                .moTa(p.getMoTa())
                .build();
    }

    public static Phong toEntity(PhongDto dto) {
        if (dto == null) return null;
        return Phong.builder()
                .id(dto.getId())
                .maPhong(dto.getMaPhong())
                .toaNhaId(dto.getToaNhaId())
                .khuId(dto.getKhuId())
                .loaiPhongId(dto.getLoaiPhongId())
                .soGiuong(dto.getSoGiuong())
                .trangThai(dto.getTrangThai())
                .moTa(dto.getMoTa())
                .build();
    }
}

