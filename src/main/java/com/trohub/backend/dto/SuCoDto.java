package com.trohub.backend.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuCoDto {
    private Long id;
    @NotBlank(message = "loai is required")
    private String loai;

    @Size(max = 2000, message = "moTa must be at most 2000 characters")
    private String moTa;

    private Long toaNhaId;

    @NotNull(message = "phongId is required")
    private Long phongId;

    @NotNull(message = "reportedBy is required")
    private Long reportedBy;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private String status;
    private java.util.List<String> imagePaths;
}

