package com.trohub.backend.dto.billing;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitPriceDto {
    private Long id;
    private String meterType;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private BigDecimal pricePerUnit;
}

