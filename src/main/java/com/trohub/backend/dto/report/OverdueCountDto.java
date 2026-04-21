package com.trohub.backend.dto.report;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverdueCountDto {
    private LocalDate asOf;
    private Long overdueCount;
}

