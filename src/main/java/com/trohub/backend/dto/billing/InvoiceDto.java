package com.trohub.backend.dto.billing;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDto {
    private Long id;
    private String invoiceNumber;
    private Long tenantId;
    private Integer periodYear;
    private Integer periodMonth;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal penaltyAmount;
    private String status;
    @Builder.Default
    private List<SubInvoiceDto> dienItems = new ArrayList<>();
    @Builder.Default
    private List<SubInvoiceDto> nuocItems = new ArrayList<>();
    @Builder.Default
    private List<com.trohub.backend.dto.billing.PaymentRecordDto> payments = new ArrayList<>();
}

