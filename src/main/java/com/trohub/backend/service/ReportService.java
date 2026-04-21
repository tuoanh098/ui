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

    // electricity summary for a given year/month; if toaNhaId is null summarize all
    com.trohub.backend.dto.report.ElectricitySummaryDto totalElectricity(Integer year, Integer month, Long toaNhaId);

    // revenue aggregated by building or room
    java.util.List<com.trohub.backend.dto.report.BuildingRevenueDto> revenueByBuilding(java.time.LocalDate from, java.time.LocalDate to);
    java.util.List<com.trohub.backend.dto.report.RoomRevenueDto> revenueByRoom(java.time.LocalDate from, java.time.LocalDate to);
    com.trohub.backend.dto.report.LandlordSummaryDto landlordSummary(Long chuTroId, java.time.LocalDate from, java.time.LocalDate to);
}

