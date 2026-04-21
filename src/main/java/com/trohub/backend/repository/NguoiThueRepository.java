package com.trohub.backend.repository;

import com.trohub.backend.modal.NguoiThue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NguoiThueRepository extends JpaRepository<NguoiThue, Long> {
    Optional<NguoiThue> findByTaiKhoanId(Long taiKhoanId);
}

