package com.trohub.backend.dto.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopTenantDto {
    private Long tenantId;
    private String hoTen;
    private Long sophong;
    private BigDecimal totalRevenue;
    private Long invoiceCount;
}

