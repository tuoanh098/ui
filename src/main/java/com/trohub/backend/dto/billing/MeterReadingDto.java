package com.trohub.backend.dto.billing;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReadingDto {
    private Long id;
    private String meterType; // ELECTRIC or WATER
    private String meterId;
    private Long tenantId;
    private Long readingValue;
    private LocalDate recordedAt;
    private Integer periodYear;
    private Integer periodMonth;
}

