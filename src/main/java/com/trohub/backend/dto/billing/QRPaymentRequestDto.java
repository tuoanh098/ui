package com.trohub.backend.dto.billing;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRPaymentRequestDto {
    @NotBlank(message = "qrCode is required")
    private String qrCode;

    @NotNull(message = "paidAmount is required")
    @Positive(message = "paidAmount must be positive")
    private BigDecimal paidAmount;

    private String externalTxnId;
}

