package com.trohub.backend.repository;

import com.trohub.backend.modal.billing.QRPaymentLog;
import com.trohub.backend.modal.billing.QRStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface QRPaymentLogRepository extends JpaRepository<QRPaymentLog, Long> {
    QRPaymentLog findByQrCode(String qrCode);
    List<QRPaymentLog> findByStatusAndExpiresAtBefore(QRStatus status, LocalDateTime time);
}

