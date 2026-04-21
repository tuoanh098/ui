package com.trohub.backend.repository;

import com.trohub.backend.dto.report.OverdueCountDto;
import com.trohub.backend.dto.report.RevenueReportDto;
import com.trohub.backend.dto.report.TopTenantDto;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReportRepository {

    @PersistenceContext
    private EntityManager em;

    public List<RevenueReportDto> revenueByPeriod(LocalDate from, LocalDate to, String groupBy) {
        String dateExpr = "DATE(issue_date)";
        if ("month".equalsIgnoreCase(groupBy)) {
            dateExpr = "DATE_FORMAT(issue_date, '%Y-%m')";
        }
        String sql = "SELECT " + dateExpr + " as period, SUM(total_amount) as total FROM hoa_don WHERE issue_date BETWEEN :fromDate AND :toDate GROUP BY " + dateExpr + " ORDER BY " + dateExpr;
        Query q = em.createNativeQuery(sql);
        q.setParameter("fromDate", java.sql.Date.valueOf(from));
        q.setParameter("toDate", java.sql.Date.valueOf(to));
        @SuppressWarnings("unchecked")
        List<Object[]> rows = (List<Object[]>) q.getResultList();
        List<RevenueReportDto> res = new ArrayList<>();
        for (Object[] r : rows) {
            String period = r[0] != null ? r[0].toString() : null;
            BigDecimal total = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
            res.add(new RevenueReportDto(period, total));
        }
        return res;
    }

    public List<TopTenantDto> topTenants(LocalDate from, LocalDate to, int limit) {
        String sql = "SELECT h.tenant_id, nt.ho_ten, nt.sophong, SUM(h.total_amount) AS total_revenue, COUNT(*) AS invoice_count " +
                "FROM hoa_don h JOIN nguoithue nt ON nt.id = h.tenant_id " +
                "WHERE h.issue_date BETWEEN :fromDate AND :toDate GROUP BY h.tenant_id, nt.ho_ten, nt.sophong ORDER BY total_revenue DESC LIMIT :limit";
        Query q = em.createNativeQuery(sql);
        q.setParameter("fromDate", java.sql.Date.valueOf(from));
        q.setParameter("toDate", java.sql.Date.valueOf(to));
        q.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = (List<Object[]>) q.getResultList();
        List<TopTenantDto> res = new ArrayList<>();
        for (Object[] r : rows) {
            Long tenantId = r[0] != null ? Long.valueOf(r[0].toString()) : null;
            String hoTen = r[1] != null ? r[1].toString() : null;
            Long sophong = r[2] != null ? Long.valueOf(r[2].toString()) : null;
            BigDecimal total = r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO;
            Long count = r[4] != null ? Long.valueOf(r[4].toString()) : 0L;
            TopTenantDto dto = TopTenantDto.builder()
                    .tenantId(tenantId)
                    .hoTen(hoTen)
                    .sophong(sophong)
                    .totalRevenue(total)
                    .invoiceCount(count)
                    .build();
            res.add(dto);
        }
        return res;
    }

    public OverdueCountDto overdueCount(LocalDate asOf) {
        String sql = "SELECT COUNT(*) FROM hoa_don WHERE status <> 'PAID' AND due_date < :asOf";
        Query q = em.createNativeQuery(sql);
        q.setParameter("asOf", java.sql.Date.valueOf(asOf));
        Object single = q.getSingleResult();
        Long count = single != null ? Long.valueOf(single.toString()) : 0L;
        return new OverdueCountDto(asOf, count);
    }

    public com.trohub.backend.dto.report.ElectricitySummaryDto totalElectricity(Integer year, Integer month, Long toaNhaId) {
        String sql;
        if (toaNhaId == null) {
            sql = "SELECT SUM(consumption) AS total_consumption, SUM(amount) AS total_amount FROM hoa_don_dien WHERE period_year = :year AND period_month = :month";
            Query q = em.createNativeQuery(sql);
            q.setParameter("year", year);
            q.setParameter("month", month);
            Object[] r = (Object[]) q.getSingleResult();
            Long totalCons = r[0] != null ? Long.valueOf(r[0].toString()) : 0L;
            java.math.BigDecimal totalAmt = r[1] != null ? new java.math.BigDecimal(r[1].toString()) : java.math.BigDecimal.ZERO;
            return com.trohub.backend.dto.report.ElectricitySummaryDto.builder().year(year).month(month).totalConsumption(totalCons).totalAmount(totalAmt).build();
        } else {
            sql = "SELECT SUM(hd.consumption) AS total_consumption, SUM(hd.amount) AS total_amount " +
                    "FROM hoa_don_dien hd JOIN hoa_don h ON hd.hoa_don_id = h.id " +
                    "JOIN nguoithue nt ON nt.id = h.tenant_id " +
                    "JOIN phong p ON p.id = nt.sophong " +
                    "JOIN toa_nha t ON t.id = p.toa_nha_id " +
                    "WHERE hd.period_year = :year AND hd.period_month = :month AND t.id = :toaNhaId";
            Query q = em.createNativeQuery(sql);
            q.setParameter("year", year);
            q.setParameter("month", month);
            q.setParameter("toaNhaId", toaNhaId);
            Object[] r = (Object[]) q.getSingleResult();
            Long totalCons = r[0] != null ? Long.valueOf(r[0].toString()) : 0L;
            java.math.BigDecimal totalAmt = r[1] != null ? new java.math.BigDecimal(r[1].toString()) : java.math.BigDecimal.ZERO;
            return com.trohub.backend.dto.report.ElectricitySummaryDto.builder().year(year).month(month).totalConsumption(totalCons).totalAmount(totalAmt).build();
        }
    }

    public java.util.List<com.trohub.backend.dto.report.BuildingRevenueDto> revenueByBuilding(LocalDate from, LocalDate to) {
        // Robust building revenue: derive building_id from tenant->phong.toa_nha_id using LEFT JOINs
        // Some invoices may not have tenant or room assigned; this query aggregates only invoices
        // that can be attributed to a building via tenant.sophong -> phong.toa_nha_id.
        String sql = "SELECT p.toa_nha_id as building_id, (SELECT ten FROM toa_nha WHERE id = p.toa_nha_id) as building_name, SUM(h.total_amount) as total_revenue " +
                "FROM hoa_don h LEFT JOIN nguoithue nt ON nt.id = h.tenant_id LEFT JOIN phong p ON p.id = nt.sophong " +
                "WHERE p.toa_nha_id IS NOT NULL AND h.issue_date BETWEEN :fromDate AND :toDate GROUP BY p.toa_nha_id ORDER BY total_revenue DESC";
        Query q = em.createNativeQuery(sql);
        q.setParameter("fromDate", java.sql.Date.valueOf(from));
        q.setParameter("toDate", java.sql.Date.valueOf(to));
        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = (java.util.List<Object[]>) q.getResultList();
        java.util.List<com.trohub.backend.dto.report.BuildingRevenueDto> res = new java.util.ArrayList<>();
        for (Object[] r : rows) {
            Long id = r[0] != null ? Long.valueOf(r[0].toString()) : null;
            String name = r[1] != null ? r[1].toString() : null;
            java.math.BigDecimal total = r[2] != null ? new java.math.BigDecimal(r[2].toString()) : java.math.BigDecimal.ZERO;
            res.add(com.trohub.backend.dto.report.BuildingRevenueDto.builder().buildingId(id).buildingName(name).totalRevenue(total).build());
        }
        return res;
    }

    public com.trohub.backend.dto.report.LandlordSummaryDto landlordSummary(Long chuTroId, LocalDate from, LocalDate to) {
        // buildingCount, roomCount, occupiedCount
        String sqlCounts = "SELECT COUNT(DISTINCT t.id) as building_count, COUNT(DISTINCT p.id) as room_count, SUM(CASE WHEN nt.sophong IS NOT NULL THEN 1 ELSE 0 END) as occupied_count " +
                "FROM toa_nha t LEFT JOIN phong p ON p.toa_nha_id = t.id LEFT JOIN nguoithue nt ON nt.sophong = p.id " +
                "WHERE t.id IN (SELECT id FROM toa_nha WHERE chu_tro_id = :chuTroId)";
        Query q = em.createNativeQuery(sqlCounts);
        q.setParameter("chuTroId", chuTroId);
        Object[] counts = (Object[]) q.getSingleResult();
        Long buildingCount = counts[0] != null ? Long.valueOf(counts[0].toString()) : 0L;
        Long roomCount = counts[1] != null ? Long.valueOf(counts[1].toString()) : 0L;
        Long occupiedCount = counts[2] != null ? Long.valueOf(counts[2].toString()) : 0L;

        // total revenue for that landlord between dates
        String sqlRevenue = "SELECT SUM(h.total_amount) FROM hoa_don h JOIN nguoithue nt ON nt.id = h.tenant_id JOIN phong p ON p.id = nt.sophong JOIN toa_nha t ON t.id = p.toa_nha_id WHERE t.chu_tro_id = :chuTroId AND h.issue_date BETWEEN :fromDate AND :toDate";
        Query q2 = em.createNativeQuery(sqlRevenue);
        q2.setParameter("chuTroId", chuTroId);
        q2.setParameter("fromDate", java.sql.Date.valueOf(from));
        q2.setParameter("toDate", java.sql.Date.valueOf(to));
        Object revObj = q2.getSingleResult();
        java.math.BigDecimal totalRevenue = revObj != null ? new java.math.BigDecimal(revObj.toString()) : java.math.BigDecimal.ZERO;

        // landlord name lookup
        String nameSql = "SELECT ten FROM chu_tro WHERE id = :chuTroId";
        Query q3 = em.createNativeQuery(nameSql);
        q3.setParameter("chuTroId", chuTroId);
        Object nameObj = q3.getSingleResult();
        String landlordName = nameObj != null ? nameObj.toString() : null;

        return com.trohub.backend.dto.report.LandlordSummaryDto.builder()
                .landlordId(chuTroId)
                .landlordName(landlordName)
                .buildingCount(buildingCount)
                .roomCount(roomCount)
                .occupiedCount(occupiedCount)
                .totalRevenue(totalRevenue)
                .build();
    }

    public java.util.List<com.trohub.backend.dto.report.RoomRevenueDto> revenueByRoom(LocalDate from, LocalDate to) {
        String sql = "SELECT p.id as room_id, p.ma_phong as room_code, SUM(h.total_amount) as total_revenue " +
                "FROM hoa_don h JOIN nguoithue nt ON nt.id = h.tenant_id " +
                "JOIN phong p ON p.id = nt.sophong " +
                "WHERE h.issue_date BETWEEN :fromDate AND :toDate GROUP BY p.id, p.ma_phong ORDER BY total_revenue DESC";
        Query q = em.createNativeQuery(sql);
        q.setParameter("fromDate", java.sql.Date.valueOf(from));
        q.setParameter("toDate", java.sql.Date.valueOf(to));
        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = (java.util.List<Object[]>) q.getResultList();
        java.util.List<com.trohub.backend.dto.report.RoomRevenueDto> res = new java.util.ArrayList<>();
        for (Object[] r : rows) {
            Long id = r[0] != null ? Long.valueOf(r[0].toString()) : null;
            String code = r[1] != null ? r[1].toString() : null;
            java.math.BigDecimal total = r[2] != null ? new java.math.BigDecimal(r[2].toString()) : java.math.BigDecimal.ZERO;
            res.add(com.trohub.backend.dto.report.RoomRevenueDto.builder().roomId(id).roomCode(code).totalRevenue(total).build());
        }
        return res;
    }
}


