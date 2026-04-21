package com.trohub.backend.dto.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportDto {
    private String period; // e.g., 2026-04-20 or 2026-04
    private BigDecimal totalAmount;
}

