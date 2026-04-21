package com.trohub.backend.mapper;

import com.trohub.backend.dto.HopDongDto;
import com.trohub.backend.modal.HopDong;

public class HopDongMapper {

    public static HopDongDto toDto(HopDong h) {
        if (h == null) return null;
        return HopDongDto.builder()
                .id(h.getId())
                .maHopDong(h.getMaHopDong())
                .phongId(h.getPhongId())
                .nguoiId(h.getNguoiId())
                .ngayBatDau(h.getNgayBatDau())
                .ngayKetThuc(h.getNgayKetThuc())
                .tienCoc(h.getTienCoc())
                .tienThue(h.getTienThue())
                .tienDienPerUnit(h.getTienDienPerUnit())
                .tienNuocFixed(h.getTienNuocFixed())
                .trangThai(h.getTrangThai())
                .build();
    }

    public static HopDong toEntity(HopDongDto dto) {
        if (dto == null) return null;
        return HopDong.builder()
                .id(dto.getId())
                .maHopDong(dto.getMaHopDong())
                .phongId(dto.getPhongId())
                .nguoiId(dto.getNguoiId())
                .ngayBatDau(dto.getNgayBatDau())
                .ngayKetThuc(dto.getNgayKetThuc())
                .tienCoc(dto.getTienCoc())
                .tienThue(dto.getTienThue())
                .tienDienPerUnit(dto.getTienDienPerUnit())
                .tienNuocFixed(dto.getTienNuocFixed())
                .trangThai(dto.getTrangThai())
                .build();
    }
}

