package com.trohub.backend.mapper;

import com.trohub.backend.dto.SuCoDto;
import com.trohub.backend.modal.SuCo;

public class SuCoMapper {
    public static SuCoDto toDto(SuCo e) {
        if (e == null) return null;
        return SuCoDto.builder()
                .id(e.getId())
                .loai(e.getLoai())
                .moTa(e.getMoTa())
                .toaNhaId(e.getToaNhaId())
                .phongId(e.getPhongId())
                .reportedBy(e.getReportedBy())
                .reportedAt(e.getReportedAt())
                .resolvedAt(e.getResolvedAt())
                .status(e.getStatus())
                .imagePaths(e.getImagePaths() == null ? java.util.List.of() : java.util.Arrays.asList(e.getImagePaths().split(",")))
                .build();
    }

    public static SuCo toEntity(SuCoDto dto) {
        if (dto == null) return null;
        return SuCo.builder()
                .id(dto.getId())
                .loai(dto.getLoai())
                .moTa(dto.getMoTa())
                .toaNhaId(dto.getToaNhaId())
                .phongId(dto.getPhongId())
                .reportedBy(dto.getReportedBy())
                .reportedAt(dto.getReportedAt())
                .resolvedAt(dto.getResolvedAt())
                .status(dto.getStatus())
                .imagePaths(dto.getImagePaths() == null ? null : String.join(",", dto.getImagePaths()))
                .build();
    }
}

