package com.trohub.backend.modal;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "phong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phong", nullable = false, unique = true)
    private String maPhong;

    @Column(name = "toa_nha_id")
    private Long toaNhaId;

    @Column(name = "khu_id")
    private Long khuId;

    @Column(name = "loai_phong_id")
    private Long loaiPhongId;

    @Column(name = "so_giuong")
    private Integer soGiuong;

    @Column(name = "trang_thai")
    private String trangThai; // TRONG, DA_THUE, MAINTENANCE

    @Column(name = "mo_ta")
    private String moTa;

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

