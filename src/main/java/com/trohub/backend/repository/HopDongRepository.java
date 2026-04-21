package com.trohub.backend.repository;

import com.trohub.backend.modal.HopDong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HopDongRepository extends JpaRepository<HopDong, Long> {
    List<HopDong> findByPhongId(Long phongId);
}

