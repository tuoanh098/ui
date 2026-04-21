package com.trohub.backend.mapper;

import com.trohub.backend.dto.KhachVaoRaDto;
import com.trohub.backend.modal.KhachVaoRa;

public class KhachVaoRaMapper {
    public static KhachVaoRaDto toDto(KhachVaoRa e) {
        if (e == null) return null;
        return KhachVaoRaDto.builder()
                .id(e.getId())
                .ten(e.getTen())
                .cmnd(e.getCmnd())
                .sdt(e.getSdt())
                .phongId(e.getPhongId())
                .loai(e.getLoai())
                .timestamp(e.getTimestamp())
                .ghiChu(e.getGhiChu())
                .approvalStatus(e.getApprovalStatus())
                .build();
    }

    public static KhachVaoRa toEntity(KhachVaoRaDto dto) {
        if (dto == null) return null;
        return KhachVaoRa.builder()
                .id(dto.getId())
                .ten(dto.getTen())
                .cmnd(dto.getCmnd())
                .sdt(dto.getSdt())
                .phongId(dto.getPhongId())
                .loai(dto.getLoai())
                .timestamp(dto.getTimestamp())
                .ghiChu(dto.getGhiChu())
                .approvalStatus(dto.getApprovalStatus())
                .build();
    }
}

