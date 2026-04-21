package com.trohub.backend.modal;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hop_dong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_hop_dong", nullable = false, unique = true)
    private String maHopDong;

    @Column(name = "phong_id")
    private Long phongId;

    @Column(name = "nguoi_id")
    private Long nguoiId;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(name = "tien_coc")
    private BigDecimal tienCoc;

    @Column(name = "tien_thue")
    private BigDecimal tienThue;

    @Column(name = "tien_dien_per_unit")
    private BigDecimal tienDienPerUnit;

    @Column(name = "tien_nuoc_fixed")
    private BigDecimal tienNuocFixed;

    @Column(name = "trang_thai")
    private String trangThai; // ACTIVE, EXPIRED, CANCELLED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

