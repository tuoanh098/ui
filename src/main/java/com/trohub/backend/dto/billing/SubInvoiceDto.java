package com.trohub.backend.dto.billing;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubInvoiceDto {
    private Long id;
    private String meterId;
    private Long startReading;
    private Long endReading;
    private Long consumption;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private Integer periodYear;
    private Integer periodMonth;
}

