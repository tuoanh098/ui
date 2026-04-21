package com.trohub.backend.repository;

import com.trohub.backend.modal.billing.HoaDonDien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoaDonDienRepository extends JpaRepository<HoaDonDien, Long> {
    List<HoaDonDien> findByHoaDonId(Long hoaDonId);
    List<HoaDonDien> findByPeriodYearAndPeriodMonth(Integer year, Integer month);
}

