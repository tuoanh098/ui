package com.trohub.backend.modal;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "bank_name")
    private String bankName;

    // optional image (logo or printable image) stored as base64 string or URL
    @Lob
    private String imageBase64;

    @Column(name = "image_url")
    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }
}

