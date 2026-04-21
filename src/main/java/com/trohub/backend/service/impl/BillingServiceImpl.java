package com.trohub.backend.service.impl;

import com.trohub.backend.dto.billing.InvoiceDto;
import com.trohub.backend.mapper.billing.BillingMapper;
import com.trohub.backend.modal.billing.*;
import com.trohub.backend.repository.*;
import com.trohub.backend.service.BillingService;
import com.trohub.backend.util.ServiceUtils;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BillingServiceImpl implements BillingService {

    private final ChiSoRepository chiSoRepository;
    private final DonGiaRepository donGiaRepository;
    private final HoaDonRepository hoaDonRepository;
    private final PhieuThuRepository phieuThuRepository;
    private final com.trohub.backend.service.BillingService billingServiceProxy;
    private final com.trohub.backend.repository.TaiKhoanRepository taiKhoanRepository;
    private final com.trohub.backend.repository.HopDongRepository hopDongRepository;

    public BillingServiceImpl(ChiSoRepository chiSoRepository, DonGiaRepository donGiaRepository, HoaDonRepository hoaDonRepository, PhieuThuRepository phieuThuRepository, @Lazy com.trohub.backend.service.BillingService billingServiceProxy, com.trohub.backend.repository.TaiKhoanRepository taiKhoanRepository, com.trohub.backend.repository.HopDongRepository hopDongRepository) {
        this.chiSoRepository = chiSoRepository;
        this.donGiaRepository = donGiaRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.phieuThuRepository = phieuThuRepository;
        this.billingServiceProxy = billingServiceProxy;
        this.taiKhoanRepository = taiKhoanRepository;
        this.hopDongRepository = hopDongRepository;
    }

    @Override
    @Transactional
    public List<InvoiceDto> generateMonthlyBills(int year, int month) {
        return ServiceUtils.exec(() -> doGenerateMonthlyBills(year, month), "generate monthly bills for " + year + "-" + month);
    }

    @Override
    @Async
    public java.util.concurrent.CompletableFuture<Void> generateMonthlyBillsAsync(int year, int month) {
        return java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                generateMonthlyBills(year, month);
            } catch (Exception ex) {
                // log via ServiceUtils by wrapping
                ServiceUtils.exec(() -> { throw ex; }, "async generate monthly bills for " + year + "-" + month);
            }
        });
    }

    private List<InvoiceDto> doGenerateMonthlyBills(int year, int month) {
        List<Long> tenants = chiSoRepository.findDistinctTenantIdsForPeriod(year, month);
        List<InvoiceDto> results = new ArrayList<>();
        // If no readings found for the period, optionally create DRAFT invoices for all tenants (manual review)
        if (tenants == null || tenants.isEmpty()) {
            // get all users who are tenants (here we consider all tai_khoan with ROLE_USER)
            this.taiKhoanRepository.findAll().stream()
                    .filter(tk -> tk.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_USER")))
                    .forEach(tk -> {
                        // create draft if not exists
                        InvoiceDto created = taoDraftHoaDon(tk.getId(), year, month);
                        results.add(created);
                    });
            return results;
        }

        for (Long tenantId : tenants) {
            // call via proxy so transactional annotation on combineSubInvoices is applied per-invoice
            InvoiceDto dto = billingServiceProxy.combineSubInvoices(tenantId, year, month);
            results.add(dto);
        }
        return results;
    }

    @Override
    @Transactional
    public InvoiceDto combineSubInvoices(Long tenantId, int year, int month) {
        return ServiceUtils.exec(() -> doCombineSubInvoices(tenantId, year, month), "combine sub-invoices for tenant " + tenantId + " period " + year + "-" + month);
    }

    @Override
    public InvoiceDto taoDraftHoaDon(Long tenantId, int year, int month) {
        return ServiceUtils.exec(() -> doTaoDraft(tenantId, year, month), "tao draft hoa don for tenant " + tenantId);
    }

    private InvoiceDto doTaoDraft(Long tenantId, int year, int month) {
        List<HoaDon> existing = hoaDonRepository.findByTenantIdAndPeriodYearAndPeriodMonth(tenantId, year, month);
        if (existing != null && !existing.isEmpty()) {
            return BillingMapper.toDto(existing.get(0));
        }
        HoaDon hoaDon = HoaDon.builder()
                .tenantId(tenantId)
                .periodYear(year)
                .periodMonth(month)
                .issueDate(LocalDate.now())
                .dueDate(YearMonth.of(year, month).atEndOfMonth().plusDays(15))
                .totalAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.DRAFT)
                .build();
        HoaDon saved = hoaDonRepository.save(hoaDon);
        return BillingMapper.toDto(saved);
    }

    private InvoiceDto doCombineSubInvoices(Long tenantId, int year, int month) {
        // if invoice exists, return it
        List<HoaDon> existing = hoaDonRepository.findByTenantIdAndPeriodYearAndPeriodMonth(tenantId, year, month);
        if (existing != null && !existing.isEmpty()) {
            return BillingMapper.toDto(existing.get(0));
        }

        List<ChiSoDienNuoc> readings = chiSoRepository.findByTenantIdAndPeriodYearAndPeriodMonth(tenantId, year, month);
        HoaDon hoaDon = HoaDon.builder()
                .tenantId(tenantId)
                .periodYear(year)
                .periodMonth(month)
                .issueDate(LocalDate.now())
                .dueDate(YearMonth.of(year, month).atEndOfMonth().plusDays(15))
                .build();

        BigDecimal total = BigDecimal.ZERO;

        boolean coThieuChiSoTruoc = false;
        for (ChiSoDienNuoc r : readings) {
            // determine previous period readings for same meter
            int prevMonth = month - 1;
            int prevYear = year;
            if (prevMonth == 0) { prevMonth = 12; prevYear = year - 1; }
            Long chiSoBatDau = null;
            List<ChiSoDienNuoc> prev = chiSoRepository.findByTenantIdAndPeriodYearAndPeriodMonth(tenantId, prevYear, prevMonth);
            if (prev != null) {
                Optional<ChiSoDienNuoc> prevSameMeter = prev.stream().filter(p -> p.getMeterId() != null && p.getMeterId().equals(r.getMeterId())).findFirst();
                if (prevSameMeter.isPresent()) chiSoBatDau = prevSameMeter.get().getReadingValue();
            }

            if (chiSoBatDau == null) {
                // Missing previous reading -> we should mark invoice as DRAFT and skip computing this sub-invoice
                coThieuChiSoTruoc = true;
                continue;
            }

            long rawConsumption = (r.getReadingValue() != null ? r.getReadingValue() : 0L) - chiSoBatDau;
            if (rawConsumption < 0) rawConsumption = 0L;
            Long consumption = Long.valueOf(rawConsumption);

            // get unit price
            BigDecimal unitPrice = BigDecimal.ZERO;
            Optional<DonGia> priceOpt = donGiaRepository.findPriceForDate(r.getMeterType(), YearMonth.of(year, month).atEndOfMonth());
            if (priceOpt.isPresent()) unitPrice = priceOpt.get().getPricePerUnit();


            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(consumption));

            if (r.getMeterType() == MeterType.ELECTRIC) {
                HoaDonDien d = HoaDonDien.builder()
                        .meterId(r.getMeterId())
                        .startReading(chiSoBatDau)
                        .endReading(r.getReadingValue())
                        .consumption(consumption)
                        .unitPrice(unitPrice)
                        .amount(amount)
                        .periodYear(year)
                        .periodMonth(month)
                        .build();
                // set owning side reference so JPA writes hoa_don_id correctly
                d.setHoaDon(hoaDon);
                hoaDon.getDienItems().add(d);
            } else {
                HoaDonNuoc n = HoaDonNuoc.builder()
                        .meterId(r.getMeterId())
                        .startReading(chiSoBatDau)
                        .endReading(r.getReadingValue())
                        .consumption(consumption)
                        .unitPrice(unitPrice)
                        .amount(amount)
                        .periodYear(year)
                        .periodMonth(month)
                        .build();
                n.setHoaDon(hoaDon);
                hoaDon.getNuocItems().add(n);
            }

            total = total.add(amount);
        }

        hoaDon.setTotalAmount(total);
        // include contract-level adjustments (rent, optional utility rates) if exists
        try {
            // find active contract for tenant
            java.util.Optional<com.trohub.backend.modal.HopDong> maybe = hopDongRepository.findAll().stream().filter(h -> h.getNguoiId() != null && h.getNguoiId().equals(tenantId) && "ACTIVE".equalsIgnoreCase(h.getTrangThai())).findFirst();
            if (maybe.isPresent()) {
                com.trohub.backend.modal.HopDong hd = maybe.get();
                // if contract defines a per-unit electricity rate, override dien item prices
                if (hd.getTienDienPerUnit() != null) {
                    java.math.BigDecimal perUnit = hd.getTienDienPerUnit();
                    // recompute dien item amounts
                    for (HoaDonDien d : hoaDon.getDienItems()) {
                        d.setUnitPrice(perUnit);
                        d.setAmount(perUnit.multiply(java.math.BigDecimal.valueOf(d.getConsumption())));
                    }
                    // recompute total from items
                    java.math.BigDecimal recomputed = java.math.BigDecimal.ZERO;
                    for (HoaDonDien d : hoaDon.getDienItems()) recomputed = recomputed.add(d.getAmount());
                    for (HoaDonNuoc n : hoaDon.getNuocItems()) recomputed = recomputed.add(n.getAmount());
                    hoaDon.setTotalAmount(recomputed);
                }

                // if contract defines fixed water charge, add as a nuoc item and to total
                if (hd.getTienNuocFixed() != null) {
                    java.math.BigDecimal fixed = hd.getTienNuocFixed();
                    HoaDonNuoc n = HoaDonNuoc.builder()
                            .meterId("FIXED_WATER")
                            .startReading(0L)
                            .endReading(0L)
                            .consumption(1L)
                            .unitPrice(fixed)
                            .amount(fixed)
                            .periodYear(year)
                            .periodMonth(month)
                            .build();
                            n.setHoaDon(hoaDon);
                            hoaDon.getNuocItems().add(n);
                    hoaDon.setTotalAmount(hoaDon.getTotalAmount().add(fixed));
                }

                // include rent
                if (hd.getTienThue() != null) {
                    java.math.BigDecimal rent = hd.getTienThue();
                    hoaDon.setTotalAmount(hoaDon.getTotalAmount().add(rent));
                }
            }
        } catch (Exception ex) {
            // ignore if hopDong lookup fails
        }
        if (coThieuChiSoTruoc) {
            hoaDon.setStatus(InvoiceStatus.DRAFT);
        } else {
            hoaDon.setStatus(InvoiceStatus.UNPAID);
        }
        HoaDon saved = hoaDonRepository.save(hoaDon);
        return BillingMapper.toDto(saved);
    }

    @Override
    public void applyDailyLateFee(Long hoaDonId, int perDayAmount) {
        ServiceUtils.exec(() -> {
            doApplyDailyLateFee(hoaDonId, perDayAmount);
            return null;
        }, "apply daily late fee for " + hoaDonId);
    }

    private void doApplyDailyLateFee(Long hoaDonId, int perDayAmount) {
        HoaDon h = hoaDonRepository.findById(hoaDonId).orElseThrow(() -> new com.trohub.backend.exception.ResourceNotFoundException("HoaDon not found"));
        // grace date is 8th of period month
        java.time.LocalDate grace = java.time.YearMonth.of(h.getPeriodYear(), h.getPeriodMonth()).atDay(8);
        java.time.LocalDate today = java.time.LocalDate.now();
        if (!today.isAfter(grace)) return; // not yet past grace

        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(grace, today);
        if (daysOverdue <= 0) return;

        // calculate how many days already applied based on penaltyAmount
        java.math.BigDecimal already = h.getPenaltyAmount() != null ? h.getPenaltyAmount() : java.math.BigDecimal.ZERO;
        long daysAlreadyApplied = 0;
        if (already.compareTo(java.math.BigDecimal.ZERO) > 0) {
            daysAlreadyApplied = already.divide(java.math.BigDecimal.valueOf(perDayAmount)).longValue();
        }
        long toApply = daysOverdue - daysAlreadyApplied;
        if (toApply <= 0) return;

        java.math.BigDecimal add = java.math.BigDecimal.valueOf(perDayAmount).multiply(java.math.BigDecimal.valueOf(toApply));
        h.setPenaltyAmount((h.getPenaltyAmount() != null ? h.getPenaltyAmount() : java.math.BigDecimal.ZERO).add(add));
        h.setTotalAmount(h.getTotalAmount().add(add));
        h.setStatus(com.trohub.backend.modal.billing.InvoiceStatus.OVERDUE);
        hoaDonRepository.save(h);
    }

    @Override
    public BigDecimal calculatePenalty(Long hoaDonId) {
        return ServiceUtils.exec(() -> doCalculatePenalty(hoaDonId), "calculate penalty for hoaDon " + hoaDonId);
    }

    private BigDecimal doCalculatePenalty(Long hoaDonId) {
        HoaDon h = hoaDonRepository.findById(hoaDonId).orElseThrow(() -> new com.trohub.backend.exception.ResourceNotFoundException("HoaDon not found"));
        BigDecimal paid = phieuThuRepository.sumPaidByHoaDonId(hoaDonId);
        BigDecimal unpaid = h.getTotalAmount().subtract(paid != null ? paid : BigDecimal.ZERO);
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        // simple 10% penalty
        BigDecimal penalty = unpaid.multiply(BigDecimal.valueOf(0.10));
        h.setPenaltyAmount(penalty);
        h.setTotalAmount(h.getTotalAmount().add(penalty));
        h.setStatus(InvoiceStatus.OVERDUE);
        hoaDonRepository.save(h);
        return penalty;
    }

    @Override
    public InvoiceDto getInvoice(Long id) {
        return ServiceUtils.exec(() -> hoaDonRepository.findById(id).map(BillingMapper::toDto).orElseThrow(() -> new com.trohub.backend.exception.ResourceNotFoundException("HoaDon not found")), "get invoice " + id);
    }

    @Override
    public List<InvoiceDto> listInvoicesForTenant(Long tenantId, int year, int month) {
        return ServiceUtils.exec(() -> hoaDonRepository.findByTenantIdAndPeriodYearAndPeriodMonth(tenantId, year, month).stream().map(BillingMapper::toDto).collect(java.util.stream.Collectors.toList()), "list invoices for tenant " + tenantId);
    }
}

