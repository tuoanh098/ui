package com.trohub.backend.modal;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "toa_nha")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToaNha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "chu_tro_id")
    private Long chuTroId;

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

