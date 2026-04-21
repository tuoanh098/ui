package com.trohub.backend.dto.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandlordSummaryDto {
    private Long landlordId;
    private String landlordName;
    private Long buildingCount;
    private Long roomCount;
    private Long occupiedCount;
    private BigDecimal totalRevenue;
}

