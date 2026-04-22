package com.trohub.backend.controller;

import com.trohub.backend.modal.billing.ChiSoDienNuoc;
import com.trohub.backend.modal.BankInfo;
import com.trohub.backend.dto.billing.InvoiceDto;
import com.trohub.backend.repository.BankInfoRepository;
import com.trohub.backend.modal.billing.MeterType;
import com.trohub.backend.repository.ChiSoRepository;
import com.trohub.backend.repository.HopDongRepository;
import com.trohub.backend.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Temporary admin endpoints to help QA / Postman testing:
 * - create readings for a tenant (so billing will produce dien/nuoc items)
 * - apply daily fee to a specific invoice immediately
 */
@RestController
@RequestMapping("/api/billing/admin")
public class BillingAdminController {

    private final ChiSoRepository chiSoRepository;
    private final BillingService billingService;
    private final com.trohub.backend.repository.DonGiaRepository donGiaRepository;
    private final com.trohub.backend.repository.HoaDonRepository hoaDonRepository;
    private final BankInfoRepository bankInfoRepository;
    private final com.trohub.backend.repository.PhieuThuRepository phieuThuRepository;
    private final HopDongRepository hopDongRepository;

    public BillingAdminController(ChiSoRepository chiSoRepository, BillingService billingService, com.trohub.backend.repository.DonGiaRepository donGiaRepository, com.trohub.backend.repository.HoaDonRepository hoaDonRepository, BankInfoRepository bankInfoRepository, com.trohub.backend.repository.PhieuThuRepository phieuThuRepository, HopDongRepository hopDongRepository) {
        this.chiSoRepository = chiSoRepository;
        this.billingService = billingService;
        this.donGiaRepository = donGiaRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.bankInfoRepository = bankInfoRepository;
        this.phieuThuRepository = phieuThuRepository;
        this.hopDongRepository = hopDongRepository;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/readings")
    public ResponseEntity<ChiSoDienNuoc> createReading(@RequestBody CreateReadingRequest req) {
        ChiSoDienNuoc r = ChiSoDienNuoc.builder()
                .meterType(req.getMeterType())
                .meterId(req.getMeterId())
                .tenantId(req.getTenantId())
                .readingValue(req.getReadingValue())
                .periodYear(req.getPeriodYear())
                .periodMonth(req.getPeriodMonth())
                .recordedAt(java.time.LocalDate.now())
                .recordedBy(req.getRecordedBy())
                .build();
        ChiSoDienNuoc saved = chiSoRepository.save(r);
        return ResponseEntity.created(URI.create("/api/billing/admin/readings/" + saved.getId())).body(saved);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/apply-daily-fee")
    public ResponseEntity<?> applyDailyFee(@RequestBody ApplyFeeRequest req) {
        billingService.applyDailyLateFee(req.getHoaDonId(), req.getPerDayAmount());
        return ResponseEntity.ok(java.util.Map.of("message", "applied"));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Long id) {
        hoaDonRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/invoices/cleanup")
    public ResponseEntity<?> cleanupInvoices(@RequestBody CleanupRequest req) {
        if (req.getTenantId() == null || req.getPeriodYear() == null || req.getPeriodMonth() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "tenantId, periodYear and periodMonth are required"));
        }
        var list = hoaDonRepository.findByTenantIdAndPeriodYearAndPeriodMonth(req.getTenantId(), req.getPeriodYear(), req.getPeriodMonth());
        hoaDonRepository.deleteAll(list);
        return ResponseEntity.ok(java.util.Map.of("deleted", list.size()));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/invoices/regenerate")
    public ResponseEntity<?> regenerateInvoice(@RequestBody CleanupRequest req) {
        if (req.getPeriodYear() == null || req.getPeriodMonth() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "periodYear and periodMonth are required"));
        }

        int year = req.getPeriodYear();
        int month = req.getPeriodMonth();

        if (req.getTenantId() != null) {
            var list = hoaDonRepository.findByTenantIdAndPeriodYearAndPeriodMonth(req.getTenantId(), year, month);
            hoaDonRepository.deleteAll(list);
            InvoiceDto dto = billingService.combineSubInvoices(req.getTenantId(), year, month);
            return ResponseEntity.ok(java.util.List.of(dto));
        }

        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        Set<Long> tenantIds = new LinkedHashSet<>();
        hopDongRepository.findAll().stream()
                .filter(h -> h.getNguoiId() != null)
                .filter(h -> h.getNgayBatDau() == null || !h.getNgayBatDau().isAfter(end))
                .filter(h -> h.getNgayKetThuc() == null || !h.getNgayKetThuc().isBefore(start))
                .filter(h -> h.getTrangThai() == null || !"CANCELLED".equalsIgnoreCase(h.getTrangThai()))
                .forEach(h -> tenantIds.add(h.getNguoiId()));

        List<InvoiceDto> regenerated = new ArrayList<>();
        for (Long tenantId : tenantIds) {
            var exists = hoaDonRepository.findByTenantIdAndPeriodYearAndPeriodMonth(tenantId, year, month);
            hoaDonRepository.deleteAll(exists);
            regenerated.add(billingService.combineSubInvoices(tenantId, year, month));
        }
        return ResponseEntity.ok(regenerated);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/invoices/{id}/simulate-paid")
    public ResponseEntity<?> simulateInvoicePaid(@PathVariable Long id) {
        var hoaDon = hoaDonRepository.findById(id).orElse(null);
        if (hoaDon == null) return ResponseEntity.notFound().build();
        try {
            java.math.BigDecimal amt = hoaDon.getTotalAmount() != null ? hoaDon.getTotalAmount() : java.math.BigDecimal.ZERO;
            com.trohub.backend.modal.billing.PhieuThu pt = com.trohub.backend.modal.billing.PhieuThu.builder()
                    .hoaDonId(hoaDon.getId())
                    .amountPaid(amt)
                    .paymentMethod(com.trohub.backend.modal.billing.PaymentMethod.MANUAL)
                    .paymentDate(java.time.LocalDateTime.now())
                    .transactionId("SIMULATED-PAID-" + java.util.UUID.randomUUID())
                    .build();
            phieuThuRepository.save(pt);

            hoaDon.setStatus(com.trohub.backend.modal.billing.InvoiceStatus.PAID);
            hoaDonRepository.save(hoaDon);
            return ResponseEntity.ok(java.util.Map.of("message", "invoice marked as paid", "hoaDonId", id));
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(BillingAdminController.class).error("Failed to simulate paid", e);
            return ResponseEntity.status(500).body(java.util.Map.of("error", "failed to simulate paid"));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/prices")
    public ResponseEntity<?> createPrice(@RequestBody CreatePriceRequest req) {
        com.trohub.backend.modal.billing.DonGia d = com.trohub.backend.modal.billing.DonGia.builder()
                .meterType(req.getMeterType())
                .pricePerUnit(java.math.BigDecimal.valueOf(req.getPricePerUnit()))
                .effectiveFrom(req.getEffectiveFrom())
                .effectiveTo(req.getEffectiveTo())
                .build();
        var saved = donGiaRepository.save(d);
        return ResponseEntity.created(URI.create("/api/billing/admin/prices/" + saved.getId())).body(saved);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @GetMapping("/bank")
    public ResponseEntity<BankInfo> getBankInfo() {
        return bankInfoRepository.findTopByOrderByIdAsc().map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping("/bank")
    public ResponseEntity<BankInfo> upsertBankInfo(@RequestBody BankInfoRequest req) {
        BankInfo b = bankInfoRepository.findTopByOrderByIdAsc().orElse(BankInfo.builder().build());
        if (req.getAccountNumber() != null) b.setAccountNumber(req.getAccountNumber());
        if (req.getOwnerName() != null) b.setOwnerName(req.getOwnerName());
        if (req.getBankName() != null) b.setBankName(req.getBankName());
        if (req.getImageBase64() != null) b.setImageBase64(req.getImageBase64());
        BankInfo saved = bankInfoRepository.save(b);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LANDLORD')")
    @PostMapping(value = "/bank/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBankImage(@RequestParam("file") MultipartFile file) {
        // validate
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body(java.util.Map.of("error", "file is required"));
        String contentType = file.getContentType();
        java.util.List<String> allowed = java.util.List.of("image/png", "image/jpeg", "image/jpg", "image/gif");
        if (contentType == null || !allowed.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "invalid file type", "allowed", allowed));
        }
        long maxBytes = 5 * 1024 * 1024; // 5 MB
        if (file.getSize() > maxBytes) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "file too large", "maxBytes", maxBytes));
        }

        try {
            // save to disk under uploads/bank
            java.nio.file.Path uploadsRoot = java.nio.file.Paths.get("uploads", "bank");
            java.nio.file.Files.createDirectories(uploadsRoot);
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String filename = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + java.util.UUID.randomUUID() + ext;
            java.nio.file.Path target = uploadsRoot.resolve(filename);
            try (java.io.InputStream in = file.getInputStream()) {
                java.nio.file.Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // build public URL path (assuming static resource mapping serves /uploads)
            String urlPath = "/uploads/bank/" + filename;

            BankInfo b = bankInfoRepository.findTopByOrderByIdAsc().orElse(BankInfo.builder().build());
            b.setImageUrl(urlPath);
            // clear base64 to avoid large DB storage
            b.setImageBase64(null);
            BankInfo saved = bankInfoRepository.save(b);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(BillingAdminController.class).error("Failed to save bank image", e);
            return ResponseEntity.status(500).body(java.util.Map.of("error", "failed to save file"));
        }
    }

    // --- request DTOs ---
    public static class CreateReadingRequest {
        private MeterType meterType;
        private String meterId;
        private Long tenantId;
        private Long readingValue;
        private Integer periodYear;
        private Integer periodMonth;
        private String recordedBy;

        public MeterType getMeterType() { return meterType; }
        public void setMeterType(MeterType meterType) { this.meterType = meterType; }
        public String getMeterId() { return meterId; }
        public void setMeterId(String meterId) { this.meterId = meterId; }
        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public Long getReadingValue() { return readingValue; }
        public void setReadingValue(Long readingValue) { this.readingValue = readingValue; }
        public Integer getPeriodYear() { return periodYear; }
        public void setPeriodYear(Integer periodYear) { this.periodYear = periodYear; }
        public Integer getPeriodMonth() { return periodMonth; }
        public void setPeriodMonth(Integer periodMonth) { this.periodMonth = periodMonth; }
        public String getRecordedBy() { return recordedBy; }
        public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    }

    public static class ApplyFeeRequest {
        private Long hoaDonId;
        private int perDayAmount;

        public Long getHoaDonId() { return hoaDonId; }
        public void setHoaDonId(Long hoaDonId) { this.hoaDonId = hoaDonId; }
        public int getPerDayAmount() { return perDayAmount; }
        public void setPerDayAmount(int perDayAmount) { this.perDayAmount = perDayAmount; }
    }

    public static class CreatePriceRequest {
        private com.trohub.backend.modal.billing.MeterType meterType;
        private double pricePerUnit;
        private java.time.LocalDate effectiveFrom;
        private java.time.LocalDate effectiveTo;

        public com.trohub.backend.modal.billing.MeterType getMeterType() { return meterType; }
        public void setMeterType(com.trohub.backend.modal.billing.MeterType meterType) { this.meterType = meterType; }
        public double getPricePerUnit() { return pricePerUnit; }
        public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
        public java.time.LocalDate getEffectiveFrom() { return effectiveFrom; }
        public void setEffectiveFrom(java.time.LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
        public java.time.LocalDate getEffectiveTo() { return effectiveTo; }
        public void setEffectiveTo(java.time.LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    }

    public static class CleanupRequest {
        private Long tenantId;
        private Integer periodYear;
        private Integer periodMonth;

        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public Integer getPeriodYear() { return periodYear; }
        public void setPeriodYear(Integer periodYear) { this.periodYear = periodYear; }
        public Integer getPeriodMonth() { return periodMonth; }
        public void setPeriodMonth(Integer periodMonth) { this.periodMonth = periodMonth; }
    }

    public static class BankInfoRequest {
        private String accountNumber;
        private String ownerName;
        private String bankName;
        private String imageBase64;

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getOwnerName() { return ownerName; }
        public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    }
}

