package com.trohub.backend.dto.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRevenueDto {
    private Long roomId;
    private String roomCode;
    private BigDecimal totalRevenue;
}


