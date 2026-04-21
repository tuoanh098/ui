package com.trohub.backend.repository;

import com.trohub.backend.modal.billing.DonGia;
import com.trohub.backend.modal.billing.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DonGiaRepository extends JpaRepository<DonGia, Long> {

    @Query("select d from DonGia d where d.meterType = :type and d.effectiveFrom <= :date and (d.effectiveTo is null or d.effectiveTo >= :date) order by d.effectiveFrom desc")
    Optional<DonGia> findPriceForDate(@Param("type") MeterType type, @Param("date") LocalDate date);
}

