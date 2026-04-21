package com.trohub.backend.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachVaoRaDto {
    private Long id;

    @NotBlank(message = "ten is required")
    private String ten;

    @NotBlank(message = "cmnd is required")
    @Size(max = 64, message = "cmnd must be at most 64 characters")
    private String cmnd;

    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "sdt must contain only digits, spaces, + or - and be 7-20 chars")
    private String sdt;

    @NotNull(message = "phongId is required")
    private Long phongId;

    private String loai;
    private LocalDateTime timestamp;
    private String ghiChu;
    private String approvalStatus;
}

