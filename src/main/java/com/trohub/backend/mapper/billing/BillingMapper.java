package com.trohub.backend.mapper.billing;

import com.trohub.backend.dto.billing.InvoiceDto;
import com.trohub.backend.dto.billing.SubInvoiceDto;
import com.trohub.backend.modal.billing.HoaDon;
import com.trohub.backend.modal.billing.HoaDonDien;
import com.trohub.backend.modal.billing.HoaDonNuoc;

import java.util.stream.Collectors;

public final class BillingMapper {

    private BillingMapper() {}

    public static InvoiceDto toDto(HoaDon h) {
        if (h == null) return null;
        InvoiceDto.InvoiceDtoBuilder b = InvoiceDto.builder()
                .id(h.getId())
                .invoiceNumber(h.getInvoiceNumber())
                .tenantId(h.getTenantId())
                .periodYear(h.getPeriodYear())
                .periodMonth(h.getPeriodMonth())
                .issueDate(h.getIssueDate())
                .dueDate(h.getDueDate())
                .totalAmount(h.getTotalAmount())
                .penaltyAmount(h.getPenaltyAmount())
                .status(h.getStatus() != null ? h.getStatus().name() : null);

        InvoiceDto dto = b.build();
        if (h.getDienItems() != null) {
            dto.setDienItems(h.getDienItems().stream().map(BillingMapper::toSubDto).collect(Collectors.toList()));
        }
        if (h.getNuocItems() != null) {
            dto.setNuocItems(h.getNuocItems().stream().map(BillingMapper::toSubDto).collect(Collectors.toList()));
        }
        return dto;
    }

    public static SubInvoiceDto toSubDto(HoaDonDien d) {
        if (d == null) return null;
        return SubInvoiceDto.builder()
                .id(d.getId())
                .meterId(d.getMeterId())
                .startReading(d.getStartReading())
                .endReading(d.getEndReading())
                .consumption(d.getConsumption())
                .unitPrice(d.getUnitPrice())
                .amount(d.getAmount())
                .periodYear(d.getPeriodYear())
                .periodMonth(d.getPeriodMonth())
                .build();
    }

    public static SubInvoiceDto toSubDto(HoaDonNuoc n) {
        if (n == null) return null;
        return SubInvoiceDto.builder()
                .id(n.getId())
                .meterId(n.getMeterId())
                .startReading(n.getStartReading())
                .endReading(n.getEndReading())
                .consumption(n.getConsumption())
                .unitPrice(n.getUnitPrice())
                .amount(n.getAmount())
                .periodYear(n.getPeriodYear())
                .periodMonth(n.getPeriodMonth())
                .build();
    }
}

