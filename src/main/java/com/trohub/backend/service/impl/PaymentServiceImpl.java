package com.trohub.backend.service.impl;

import com.trohub.backend.dto.billing.*;
import com.trohub.backend.modal.billing.*;
import com.trohub.backend.modal.BankInfo;
import com.trohub.backend.repository.*;
import com.trohub.backend.repository.BankInfoRepository;
import com.trohub.backend.repository.NguoiThueRepository;
import com.trohub.backend.service.PaymentService;
import com.trohub.backend.util.ServiceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final QRPaymentLogRepository qrRepo;
    private final HoaDonRepository hoaDonRepo;
    private final PhieuThuRepository phieuThuRepo;
    private final BankInfoRepository bankInfoRepository;
    private final NguoiThueRepository nguoiThueRepository;
    public PaymentServiceImpl(QRPaymentLogRepository qrRepo, HoaDonRepository hoaDonRepo, PhieuThuRepository phieuThuRepo, BankInfoRepository bankInfoRepository, NguoiThueRepository nguoiThueRepository) {
        this.qrRepo = qrRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.phieuThuRepo = phieuThuRepo;
        this.bankInfoRepository = bankInfoRepository;
        this.nguoiThueRepository = nguoiThueRepository;
    }

    @Override
    @Transactional
    public QRCreateResponseDto taoQrChoHoaDon(QRCreateRequestDto request) {
        return ServiceUtils.exec(() -> doTaoQr(request), "tao QR cho hoa don " + request.getHoaDonId());
    }

    private QRCreateResponseDto doTaoQr(QRCreateRequestDto request) {
        HoaDon hoaDon = hoaDonRepo.findById(request.getHoaDonId()).orElseThrow(() -> new com.trohub.backend.exception.ResourceNotFoundException("HoaDon not found"));
        BigDecimal soTien = request.getAmount() != null ? request.getAmount() : hoaDon.getTotalAmount();
        String maQr = "QR-" + UUID.randomUUID();
        // (previously collected tenant info for other QR types; not used for VIETQR)

        // build VietQR-like JSON payload (simplified for FE testing)
        BankInfo bank = bankInfoRepository.findTopByOrderByIdAsc().orElse(null);
        String account = bank != null && bank.getAccountNumber() != null ? bank.getAccountNumber() : "5907205384680";
        String owner = bank != null && bank.getOwnerName() != null ? bank.getOwnerName() : "VU TU OANH";
        String bankName = bank != null && bank.getBankName() != null ? bank.getBankName() : "AGRIBANK";

        // try to fetch tenant info to include name/room in payload
        String tenantName = null;
        String room = null;
        if (hoaDon.getTenantId() != null) {
            var maybe = nguoiThueRepository.findById(hoaDon.getTenantId());
            if (maybe.isPresent()) {
                var nt = maybe.get();
                tenantName = nt.getHoTen();
                room = nt.getSophong() != null ? String.valueOf(nt.getSophong()) : null;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"VIETQR\"");
        sb.append(",\"account\":\"").append(account).append('\"');
        sb.append(",\"name\":\"").append(owner).append('\"');
        sb.append(",\"bank\":\"").append(bankName).append('\"');
        if (tenantName != null) sb.append(",\"tenantName\":\"").append(tenantName).append('\"');
        if (room != null) sb.append(",\"room\":\"").append(room).append('\"');
        sb.append(",\"invoice\":").append(hoaDon.getId());
        sb.append(",\"amt\":").append(soTien);
        sb.append('}');
        String payload = sb.toString();
        LocalDateTime hetHan = LocalDateTime.now().plusMinutes(request.getTtlMinutes() != null ? request.getTtlMinutes() : 30);

        QRPaymentLog log = QRPaymentLog.builder()
                .hoaDonId(hoaDon.getId())
                .qrCode(maQr)
                .qrPayload(payload)
                .expectedAmount(soTien)
                .expiresAt(hetHan)
                .status(QRStatus.CREATED)
                .build();
        qrRepo.save(log);

        // generate QR image (PNG) as base64 data URL
        String dataUrl = null;
        try {
            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix matrix = qrWriter.encode(payload, BarcodeFormat.QR_CODE, 400, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            dataUrl = "data:image/png;base64," + base64;
        } catch (Exception e) {
            // ignore image generation errors but keep payload
        }

        QRCreateResponseDto.QRCreateResponseDtoBuilder builder = QRCreateResponseDto.builder()
                .qrCode(maQr)
                .qrPayload(payload)
                .expiresAt(hetHan)
                .expectedAmount(soTien)
                .invoiceNumber(hoaDon.getInvoiceNumber())
                .qrImageDataUrl(dataUrl)
                ;

        if (bank != null) {
            // prefer serving uploaded URL if present
            if (bank.getImageUrl() != null) builder.bankImageUrl(bank.getImageUrl());
            else if (bank.getImageBase64() != null) builder.bankImageUrl(bank.getImageBase64());
        }

        return builder.build();
    }

    @Override
    @Transactional
    public QRPaymentResultDto thanhToanBangQr(String qrCode, String externalTxnId, BigDecimal soTien) {
        return ServiceUtils.exec(() -> doThanhToan(qrCode, externalTxnId, soTien), "thanh toan QR " + qrCode);
    }

    @Override
    public com.trohub.backend.dto.billing.PaymentRecordDto createManualPayment(Long hoaDonId, com.trohub.backend.dto.billing.ManualPaymentRequestDto req, String createdBy) {
        return ServiceUtils.exec(() -> doCreateManualPayment(hoaDonId, req, createdBy), "manual payment for hoaDon " + hoaDonId);
    }

    private com.trohub.backend.dto.billing.PaymentRecordDto doCreateManualPayment(Long hoaDonId, com.trohub.backend.dto.billing.ManualPaymentRequestDto req, String createdBy) {
        HoaDon hoaDon = hoaDonRepo.findById(hoaDonId).orElseThrow(() -> new com.trohub.backend.exception.ResourceNotFoundException("HoaDon not found"));
        java.math.BigDecimal amt = req.getAmount();

        PhieuThu pt = PhieuThu.builder()
                .hoaDonId(hoaDonId)
                .amountPaid(amt)
                .paymentMethod(req.getPaymentMethod() != null ? com.trohub.backend.modal.billing.PaymentMethod.valueOf(req.getPaymentMethod()) : com.trohub.backend.modal.billing.PaymentMethod.MANUAL)
                .paymentDate(req.getPaymentDate() != null ? req.getPaymentDate() : java.time.LocalDateTime.now())
                .transactionId(req.getTransactionId())
                .createdBy(createdBy)
                .build();
        phieuThuRepo.save(pt);

        // recalc
        java.math.BigDecimal paidSoFar = phieuThuRepo.sumPaidByHoaDonId(hoaDonId);
        java.math.BigDecimal conNo = hoaDon.getTotalAmount().subtract(paidSoFar != null ? paidSoFar : java.math.BigDecimal.ZERO);
        if (conNo.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            hoaDon.setStatus(com.trohub.backend.modal.billing.InvoiceStatus.PAID);
        } else {
            hoaDon.setStatus(com.trohub.backend.modal.billing.InvoiceStatus.PARTIALLY_PAID);
        }
        hoaDonRepo.save(hoaDon);

        return com.trohub.backend.dto.billing.PaymentRecordDto.builder()
                .id(pt.getId())
                .amountPaid(pt.getAmountPaid())
                .paymentMethod(pt.getPaymentMethod() != null ? pt.getPaymentMethod().name() : null)
                .paymentDate(pt.getPaymentDate())
                .transactionId(pt.getTransactionId())
                .createdBy(pt.getCreatedBy())
                .createdAt(pt.getCreatedAt())
                .build();
    }

    @Override
    public java.util.List<com.trohub.backend.dto.billing.PaymentRecordDto> listPaymentsForInvoice(Long hoaDonId) {
        return ServiceUtils.exec(() -> doListPaymentsForInvoice(hoaDonId), "list payments for hoaDon " + hoaDonId);
    }

    private java.util.List<com.trohub.backend.dto.billing.PaymentRecordDto> doListPaymentsForInvoice(Long hoaDonId) {
        java.util.List<PhieuThu> list = phieuThuRepo.findByHoaDonId(hoaDonId);
        java.util.List<com.trohub.backend.dto.billing.PaymentRecordDto> out = new java.util.ArrayList<>();
        if (list != null) {
            for (PhieuThu p : list) {
                out.add(com.trohub.backend.dto.billing.PaymentRecordDto.builder()
                        .id(p.getId())
                        .amountPaid(p.getAmountPaid())
                        .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null)
                        .paymentDate(p.getPaymentDate())
                        .transactionId(p.getTransactionId())
                        .createdBy(p.getCreatedBy())
                        .createdAt(p.getCreatedAt())
                        .build());
            }
        }
        return out;
    }

    private QRPaymentResultDto doThanhToan(String qrCode, String externalTxnId, BigDecimal soTien) {
        QRPaymentLog log = qrRepo.findByQrCode(qrCode);
        if (log == null) throw new com.trohub.backend.exception.ResourceNotFoundException("QR not found");
        if (log.getStatus() == QRStatus.PAID) throw new com.trohub.backend.exception.BadRequestException("QR already paid");
        if (log.getExpiresAt() != null && log.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.setStatus(QRStatus.EXPIRED);
            qrRepo.save(log);
            throw new com.trohub.backend.exception.BadRequestException("QR expired");
        }

        // create phieu thu
        PhieuThu pt = PhieuThu.builder()
                .hoaDonId(log.getHoaDonId())
                .amountPaid(soTien)
                .paymentMethod(PaymentMethod.QR)
                .paymentDate(LocalDateTime.now())
                .transactionId(externalTxnId)
                .build();
        phieuThuRepo.save(pt);

        // update QR and HoaDon status
        log.setStatus(QRStatus.PAID);
        log.setPaidAt(LocalDateTime.now());
        log.setTransactionId(externalTxnId);
        qrRepo.save(log);

        // update hoaDon
        BigDecimal paidSoFar = phieuThuRepo.sumPaidByHoaDonId(log.getHoaDonId());
        HoaDon hd = hoaDonRepo.findById(log.getHoaDonId()).orElseThrow(() -> new com.trohub.backend.exception.ResourceNotFoundException("HoaDon not found"));
        BigDecimal conNo = hd.getTotalAmount().subtract(paidSoFar != null ? paidSoFar : BigDecimal.ZERO);
        if (conNo.compareTo(BigDecimal.ZERO) <= 0) {
            hd.setStatus(InvoiceStatus.PAID);
        } else {
            hd.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        hoaDonRepo.save(hd);

        return QRPaymentResultDto.builder()
                .qrCode(qrCode)
                .transactionId(externalTxnId)
                .status(log.getStatus().name())
                .paidAt(log.getPaidAt())
                .amountPaid(soTien)
                .build();
    }
}

