package com.trohub.backend.dto.billing;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCreateResponseDto {
    private String qrCode;
    private String qrPayload;
    private LocalDateTime expiresAt;
    private BigDecimal expectedAmount;
    private String invoiceNumber;
    private String qrImageDataUrl; // data:image/png;base64,...
    private String bankImageUrl; // URL to bank logo or data URL
}

