package com.trohub.backend.repository;

import com.trohub.backend.modal.ChuTro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChuTroRepository extends JpaRepository<ChuTro, Long> {
	Optional<ChuTro> findByTaiKhoanId(Long taiKhoanId);
}

