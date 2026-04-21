package com.trohub.backend.dto.billing;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRPaymentResultDto {
    private String qrCode;
    private String transactionId;
    private String status;
    private LocalDateTime paidAt;
    private BigDecimal amountPaid;
}

