package com.trohub.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChuTroDto {
    private Long id;
    private String ten;
    private String email;
    private String sdt;
    private String diaChi;
    private Long taiKhoanId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

