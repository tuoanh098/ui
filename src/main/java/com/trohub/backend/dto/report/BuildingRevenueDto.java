package com.trohub.backend.dto.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingRevenueDto {
    private Long buildingId;
    private String buildingName;
    private BigDecimal totalRevenue;
}


