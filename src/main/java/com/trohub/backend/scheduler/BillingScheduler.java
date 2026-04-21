package com.trohub.backend.scheduler;

import com.trohub.backend.modal.billing.HoaDon;
import com.trohub.backend.modal.billing.InvoiceStatus;
import com.trohub.backend.modal.billing.QRStatus;
import com.trohub.backend.repository.HoaDonRepository;
import com.trohub.backend.repository.QRPaymentLogRepository;
import com.trohub.backend.service.BillingService;
import com.trohub.backend.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class BillingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BillingScheduler.class);

    private final BillingService billingService;
    private final HoaDonRepository hoaDonRepository;
    private final QRPaymentLogRepository qrRepo;

    public BillingScheduler(BillingService billingService, HoaDonRepository hoaDonRepository, QRPaymentLogRepository qrRepo) {
        this.billingService = billingService;
        this.hoaDonRepository = hoaDonRepository;
        this.qrRepo = qrRepo;
    }

    // Monthly generation: run on 1st day of month at 02:00 to generate previous month's bills
    @Scheduled(cron = "0 0 2 1 * ?")
    public void monthlyGenerate() {
        ServiceUtils.exec(() -> {
            LocalDate now = LocalDate.now();
            LocalDate prev = now.minusMonths(1);
            int year = prev.getYear();
            int month = prev.getMonthValue();
            logger.info("Scheduled monthlyGenerate for {}-{}", year, month);
            billingService.generateMonthlyBills(year, month);
            return null;
        }, "scheduled monthly generation");
    }

    // Daily overdue check at 03:00
    @Scheduled(cron = "0 0 3 * * ?")
    public void dailyOverdueCheck() {
        ServiceUtils.exec(() -> {
            LocalDate today = LocalDate.now();
            List<HoaDon> overdue = hoaDonRepository.findByStatusInAndDueDateBefore(Arrays.asList(InvoiceStatus.UNPAID, InvoiceStatus.PARTIALLY_PAID), today);
            for (HoaDon h : overdue) {
                try {
                    // apply daily late fee of 100000 VND/day (business rule)
                    billingService.applyDailyLateFee(h.getId(), 100000);
                } catch (Exception ex) {
                    logger.error("Failed to apply daily late fee for hoaDon {}: {}", h.getId(), ex.getMessage());
                }
                // optional: set WARNING one day before grace date
                try {
                    java.time.LocalDate grace = java.time.YearMonth.of(h.getPeriodYear(), h.getPeriodMonth()).atDay(8);
                    if (java.time.LocalDate.now().isEqual(grace.minusDays(1))) {
                        h.setStatus(InvoiceStatus.OVERDUE); // or a custom WARNING status if you add one
                        hoaDonRepository.save(h);
                    }
                } catch (Exception ignore) {}
            }
            return null;
        }, "daily overdue check");
    }

    // QR expiry cleanup hourly
    @Scheduled(cron = "0 0 * * * ?")
    public void qrExpiryCleanup() {
        ServiceUtils.exec(() -> {
            List<com.trohub.backend.modal.billing.QRPaymentLog> expired = qrRepo.findByStatusAndExpiresAtBefore(QRStatus.CREATED, LocalDateTime.now());
            for (com.trohub.backend.modal.billing.QRPaymentLog q : expired) {
                q.setStatus(QRStatus.EXPIRED);
                q.setPaidAt(null);
                qrRepo.save(q);
            }
            return null;
        }, "qr expiry cleanup");
    }
}

