package com.trohub.backend.service;

import com.trohub.backend.dto.billing.InvoiceDto;

import java.math.BigDecimal;
import java.util.List;

public interface BillingService {
    List<InvoiceDto> generateMonthlyBills(int year, int month);
    java.util.concurrent.CompletableFuture<Void> generateMonthlyBillsAsync(int year, int month);

    InvoiceDto combineSubInvoices(Long tenantId, int year, int month);

    List<InvoiceDto> listInvoicesForTenant(Long tenantId, int year, int month);
    InvoiceDto taoDraftHoaDon(Long tenantId, int year, int month);

    BigDecimal calculatePenalty(Long hoaDonId);
    void applyDailyLateFee(Long hoaDonId, int perDayAmount);

    InvoiceDto getInvoice(Long id);
}

