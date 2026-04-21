package com.trohub.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhongDto {
    private Long id;

    @NotBlank(message = "maPhong is required")
    private String maPhong;

    @NotNull(message = "toaNhaId is required")
    private Long toaNhaId;

    private Long khuId;
    private Long loaiPhongId;

    @NotNull(message = "soGiuong is required")
    @Min(value = 1, message = "soGiuong must be >= 1")
    private Integer soGiuong;

    private String trangThai;
    private String moTa;
}

