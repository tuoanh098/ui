package com.trohub.backend.controller;

import com.trohub.backend.dto.billing.*;
import com.trohub.backend.repository.TaiKhoanRepository;
import com.trohub.backend.service.BillingService;
import com.trohub.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;
    private final PaymentService paymentService;
    private final TaiKhoanRepository taiKhoanRepository;

    public BillingController(BillingService billingService, PaymentService paymentService, TaiKhoanRepository taiKhoanRepository) {
        this.billingService = billingService;
        this.paymentService = paymentService;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN','ROLE_BILLING_STAFF')")
    public ResponseEntity<List<InvoiceDto>> listInvoices(@RequestParam(required = false) Integer nam, @RequestParam(required = false) Integer thang, @RequestParam(required = false) Long tenantId) {
        int namHienTai = java.time.Year.now().getValue();
        int thangHienTai = java.time.LocalDate.now().getMonthValue();
        int year = nam != null ? nam : namHienTai;
        int month = thang != null ? thang : thangHienTai;

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrStaff = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_BILLING_STAFF"));

        if (isAdminOrStaff) {
            if (tenantId != null) {
                return ResponseEntity.ok(billingService.listInvoicesForTenant(tenantId, year, month));
            }
            // if admin/staff and no tenantId => generate all (for simplicity reuse generateMonthlyBills)
            return ResponseEntity.ok(billingService.generateMonthlyBills(year, month));
        } else {
            // normal user: only their own invoices
            String username = auth.getName();
            Long id = taiKhoanRepository.findByUsername(username).map(t -> t.getId()).orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(billingService.listInvoicesForTenant(id, year, month));
        }
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<InvoiceDto> getInvoice(@PathVariable Long id) {
        InvoiceDto dto = billingService.getInvoice(id);
        // attach payment history
        try {
            var payments = paymentService.listPaymentsForInvoice(id);
            dto.setPayments(payments);
        } catch (Exception ex) {
            // log and continue
            org.slf4j.LoggerFactory.getLogger(BillingController.class).warn("Failed to attach payments for invoice {}: {}", id, ex.getMessage());
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BILLING_STAFF')")
    public ResponseEntity<List<InvoiceDto>> generate(@RequestParam String ky) {
        String[] parts = ky.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        List<InvoiceDto> ds = billingService.generateMonthlyBills(year, month);
        return ResponseEntity.ok(ds);
    }

    @PostMapping("/generate-async")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BILLING_STAFF')")
    public ResponseEntity<?> generateAsync(@RequestParam String ky) {
        String[] parts = ky.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        billingService.generateMonthlyBillsAsync(year, month);
        return ResponseEntity.accepted().body(java.util.Map.of("message", "Invoice generation started", "year", year, "month", month));
    }

    @PostMapping("/qr/create")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BILLING_STAFF','ROLE_USER')")
    public ResponseEntity<QRCreateResponseDto> taoQr(@jakarta.validation.Valid @RequestBody QRCreateRequestDto yeuCau) {
        return ResponseEntity.ok(paymentService.taoQrChoHoaDon(yeuCau));
    }

    @GetMapping("/invoices/{id}/payments")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN','ROLE_BILLING_STAFF')")
    public ResponseEntity<java.util.List<com.trohub.backend.dto.billing.PaymentRecordDto>> getInvoicePayments(@PathVariable Long id) {
        // authorization: if normal user, ensure invoice belongs to them
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrStaff = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_BILLING_STAFF"));
        if (!isAdminOrStaff) {
            String username = auth.getName();
            Long userId = taiKhoanRepository.findByUsername(username).map(t -> t.getId()).orElseThrow(() -> new RuntimeException("User not found"));
            // check invoice ownership
            InvoiceDto inv = billingService.getInvoice(id);
            if (!inv.getTenantId().equals(userId)) throw new org.springframework.security.access.AccessDeniedException("Not allowed");
        }
        return ResponseEntity.ok(paymentService.listPaymentsForInvoice(id));
    }

    @PostMapping("/invoices/{id}/payments/manual")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BILLING_STAFF')")
    public ResponseEntity<com.trohub.backend.dto.billing.PaymentRecordDto> createManualPayment(@PathVariable Long id, @jakarta.validation.Valid @RequestBody com.trohub.backend.dto.billing.ManualPaymentRequestDto req) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";
        var rec = paymentService.createManualPayment(id, req, username);
        return ResponseEntity.ok(rec);
    }

    @PostMapping("/draft")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BILLING_STAFF')")
    public ResponseEntity<InvoiceDto> taoDraft(@jakarta.validation.Valid @RequestBody com.trohub.backend.dto.billing.DraftRequestDto yeuCau) {
        int nam = yeuCau.getNam() != null ? yeuCau.getNam() : java.time.Year.now().getValue();
        int thang = yeuCau.getThang() != null ? yeuCau.getThang() : java.time.LocalDate.now().getMonthValue();
        InvoiceDto dto = billingService.taoDraftHoaDon(yeuCau.getTenantId(), nam, thang);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/payments/qr/simulate")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<QRPaymentResultDto> thanhToanQr(@jakarta.validation.Valid @RequestBody QRPaymentRequestDto yeuCau) {
        QRPaymentResultDto rs = paymentService.thanhToanBangQr(yeuCau.getQrCode(), yeuCau.getExternalTxnId(), yeuCau.getPaidAmount());
        return ResponseEntity.ok(rs);
    }
}

