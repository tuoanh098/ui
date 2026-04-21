package com.trohub.backend.modal.billing;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hoa_don_nuoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonNuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hoa_don_id")
    private com.trohub.backend.modal.billing.HoaDon hoaDon;

    private String meterId;
    private Long startReading;
    private Long endReading;
    private Long consumption;
    private BigDecimal unitPrice;
    private BigDecimal amount;

    private Integer periodYear;
    private Integer periodMonth;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); }
}

