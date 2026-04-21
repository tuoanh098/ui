package com.trohub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDongDto {
    private Long id;

    @NotBlank(message = "maHopDong is required")
    private String maHopDong;

    @NotNull(message = "phongId is required")
    private Long phongId;

    @NotNull(message = "nguoiId is required")
    private Long nguoiId;

    @NotNull(message = "ngayBatDau is required")
    private LocalDate ngayBatDau;

    private LocalDate ngayKetThuc;

    @PositiveOrZero(message = "tienCoc must be >= 0")
    private BigDecimal tienCoc;

    @NotNull(message = "tienThue is required")
    private BigDecimal tienThue;

    // optional: per-unit electric rate defined in contract
    private BigDecimal tienDienPerUnit;

    // optional: fixed water charge defined in contract
    private BigDecimal tienNuocFixed;

    private String trangThai;
}

