package com.trohub.backend.modal;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "su_co")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuCo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loai;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "toa_nha_id")
    private Long toaNhaId;

    @Column(name = "phong_id")
    private Long phongId;

    @Column(name = "reported_by")
    private Long reportedBy;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    private String status; // OPEN, RESOLVED

    @Column(name = "image_paths", columnDefinition = "TEXT")
    private String imagePaths; // comma-separated paths

    @PrePersist
    public void prePersist() {
        if (reportedAt == null) reportedAt = LocalDateTime.now();
    }
}


