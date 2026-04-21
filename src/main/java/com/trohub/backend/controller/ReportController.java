package com.trohub.backend.controller;

import com.trohub.backend.dto.report.OverdueCountDto;
import com.trohub.backend.dto.report.RevenueReportDto;
import com.trohub.backend.dto.report.TopTenantDto;
import com.trohub.backend.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_BILLING_STAFF','ROLE_ADMIN')")
    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueReportDto>> revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(reportService.revenueByPeriod(from, to, groupBy));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_BILLING_STAFF','ROLE_ADMIN')")
    @GetMapping("/top-tenants")
    public ResponseEntity<List<TopTenantDto>> topTenants(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.topTenants(from, to, limit));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_BILLING_STAFF','ROLE_ADMIN')")
    @GetMapping("/overdue")
    public ResponseEntity<OverdueCountDto> overdue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return ResponseEntity.ok(reportService.overdueCount(asOf));
    }
}

