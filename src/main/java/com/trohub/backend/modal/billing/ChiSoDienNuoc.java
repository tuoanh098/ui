package com.trohub.backend.modal.billing;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chi_so")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiSoDienNuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MeterType meterType;

    private String meterId;

    @Column(name = "tenant_id")
    private Long tenantId;

    private Long readingValue;
    private LocalDate recordedAt;

    @Column(name = "period_year")
    private Integer periodYear;

    @Column(name = "period_month")
    private Integer periodMonth;

    private String recordedBy;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); }
}

