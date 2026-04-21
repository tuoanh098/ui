package com.trohub.backend.dto.billing;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRecordDto {
    private Long id;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String transactionId;
    private String createdBy;
    private LocalDateTime createdAt;
}

