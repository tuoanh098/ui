package com.trohub.backend.service;

import com.trohub.backend.dto.report.OverdueCountDto;
import com.trohub.backend.dto.report.RevenueReportDto;
import com.trohub.backend.dto.report.TopTenantDto;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<RevenueReportDto> revenueByPeriod(LocalDate from, LocalDate to, String groupBy);
    List<TopTenantDto> topTenants(LocalDate from, LocalDate to, int limit);
    OverdueCountDto overdueCount(LocalDate asOf);
}

