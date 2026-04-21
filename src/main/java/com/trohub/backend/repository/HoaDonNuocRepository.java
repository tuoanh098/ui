package com.trohub.backend.repository;

import com.trohub.backend.modal.billing.HoaDonNuoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoaDonNuocRepository extends JpaRepository<HoaDonNuoc, Long> {
    List<HoaDonNuoc> findByHoaDonId(Long hoaDonId);
    List<HoaDonNuoc> findByPeriodYearAndPeriodMonth(Integer year, Integer month);
}

