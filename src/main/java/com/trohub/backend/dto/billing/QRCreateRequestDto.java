package com.trohub.backend.dto.billing;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCreateRequestDto {
    @jakarta.validation.constraints.NotNull(message = "hoaDonId is required")
    @JsonAlias({"invoiceId","invoice_id"})
    private Long hoaDonId;

    // amount is optional; if omitted the server will use the invoice total
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    private Integer ttlMinutes;
}

