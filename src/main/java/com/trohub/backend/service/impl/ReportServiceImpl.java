package com.trohub.backend.service.impl;

import com.trohub.backend.dto.report.OverdueCountDto;
import com.trohub.backend.dto.report.RevenueReportDto;
import com.trohub.backend.dto.report.TopTenantDto;
import com.trohub.backend.repository.ReportRepository;
import com.trohub.backend.service.ReportService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    @Cacheable(value = "reports", key = "'revenue_'+#from+'_'+#to+'_'+#groupBy")
    public List<RevenueReportDto> revenueByPeriod(LocalDate from, LocalDate to, String groupBy) {
        return reportRepository.revenueByPeriod(from, to, groupBy);
    }

    @Override
    @Cacheable(value = "reports", key = "'topTenants_'+#from+'_'+#to+'_'+#limit")
    public List<TopTenantDto> topTenants(LocalDate from, LocalDate to, int limit) {
        return reportRepository.topTenants(from, to, limit);
    }

    @Override
    @Cacheable(value = "reports", key = "'overdue_'+#asOf")
    public OverdueCountDto overdueCount(LocalDate asOf) {
        return reportRepository.overdueCount(asOf);
    }

    @Override
    public com.trohub.backend.dto.report.ElectricitySummaryDto totalElectricity(Integer year, Integer month, Long toaNhaId) {
        return reportRepository.totalElectricity(year, month, toaNhaId);
    }

    @Override
    public java.util.List<com.trohub.backend.dto.report.BuildingRevenueDto> revenueByBuilding(LocalDate from, LocalDate to) {
        return reportRepository.revenueByBuilding(from, to);
    }

    @Override
    public java.util.List<com.trohub.backend.dto.report.RoomRevenueDto> revenueByRoom(LocalDate from, LocalDate to) {
        return reportRepository.revenueByRoom(from, to);
    }

    @Override
    public com.trohub.backend.dto.report.LandlordSummaryDto landlordSummary(Long chuTroId, LocalDate from, LocalDate to) {
        return reportRepository.landlordSummary(chuTroId, from, to);
    }
}

