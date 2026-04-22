package com.trohub.backend.repository;

import com.trohub.backend.modal.billing.HoaDon;
import com.trohub.backend.modal.billing.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {

    List<HoaDon> findByTenantIdAndPeriodYearAndPeriodMonth(Long tenantId, Integer year, Integer month);
    List<HoaDon> findByPeriodYearAndPeriodMonth(Integer year, Integer month);

    List<HoaDon> findByStatusInAndDueDateBefore(List<InvoiceStatus> statuses, LocalDate date);

    HoaDon findByInvoiceNumber(String invoiceNumber);

    @Query("select h from HoaDon h where h.tenantId = :tenantId and h.periodYear = :y and h.periodMonth = :m")
    List<HoaDon> findForTenantPeriod(@Param("tenantId") Long tenantId, @Param("y") Integer year, @Param("m") Integer month);
}

