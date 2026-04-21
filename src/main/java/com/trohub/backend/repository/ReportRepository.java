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
        String sql = "SELECT " + dateExpr + " as period, SUM(total_amount) as total FROM hoa_don WHERE issue_date BETWEEN :from AND :to GROUP BY " + dateExpr + " ORDER BY " + dateExpr;
        Query q = em.createNativeQuery(sql);
        q.setParameter("from", java.sql.Date.valueOf(from));
        q.setParameter("to", java.sql.Date.valueOf(to));
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
                "WHERE h.issue_date BETWEEN :from AND :to GROUP BY h.tenant_id, nt.ho_ten, nt.sophong ORDER BY total_revenue DESC LIMIT :limit";
        Query q = em.createNativeQuery(sql);
        q.setParameter("from", java.sql.Date.valueOf(from));
        q.setParameter("to", java.sql.Date.valueOf(to));
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
}


