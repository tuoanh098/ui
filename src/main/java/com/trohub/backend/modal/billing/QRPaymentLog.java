package com.trohub.backend.modal.billing;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "qr_payment_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRPaymentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hoa_don_id")
    private Long hoaDonId;

    private String qrCode;
    @Lob
    private String qrPayload;
    private BigDecimal expectedAmount;

    @Enumerated(EnumType.STRING)
    private QRStatus status = QRStatus.CREATED;

    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime paidAt;
    private String transactionId;

    @PrePersist
    public void prePersist() { generatedAt = LocalDateTime.now(); }
}

