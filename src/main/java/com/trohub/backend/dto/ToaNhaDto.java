package com.trohub.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToaNhaDto {
    private Long id;
    private String ten;
    private String diaChi;
    private Long chuTroId;
    // optional stats
    private Long roomCount;
    private Long occupiedCount;
}

