package com.trohub.backend.dto.billing;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceiptDto {
    private Long id;
    private Long hoaDonId;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String transactionId;
}

