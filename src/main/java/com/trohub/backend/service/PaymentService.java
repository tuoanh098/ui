package com.trohub.backend.service;

import com.trohub.backend.dto.billing.QRCreateResponseDto;
import com.trohub.backend.dto.billing.QRCreateRequestDto;
import com.trohub.backend.dto.billing.QRPaymentResultDto;

public interface PaymentService {
    QRCreateResponseDto taoQrChoHoaDon(QRCreateRequestDto request);
    QRPaymentResultDto thanhToanBangQr(String qrCode, String externalTxnId, java.math.BigDecimal soTien);
    com.trohub.backend.dto.billing.PaymentRecordDto createManualPayment(Long hoaDonId, com.trohub.backend.dto.billing.ManualPaymentRequestDto req, String createdBy);
    java.util.List<com.trohub.backend.dto.billing.PaymentRecordDto> listPaymentsForInvoice(Long hoaDonId);
}

