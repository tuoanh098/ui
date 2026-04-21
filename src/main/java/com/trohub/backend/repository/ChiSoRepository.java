package com.trohub.backend.repository;

import com.trohub.backend.modal.billing.ChiSoDienNuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChiSoRepository extends JpaRepository<ChiSoDienNuoc, Long> {
    List<ChiSoDienNuoc> findByTenantIdAndPeriodYearAndPeriodMonth(Long tenantId, Integer year, Integer month);

    @Query("select distinct c.tenantId from ChiSoDienNuoc c where c.periodYear = :y and c.periodMonth = :m")
    List<Long> findDistinctTenantIdsForPeriod(@Param("y") Integer year, @Param("m") Integer month);
}

