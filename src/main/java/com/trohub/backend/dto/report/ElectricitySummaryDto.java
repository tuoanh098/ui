package com.trohub.backend.dto.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectricitySummaryDto {
    private Integer year;
    private Integer month;
    private Long totalConsumption; // in units
    private BigDecimal totalAmount; // money
}


