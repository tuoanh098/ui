package com.trohub.backend.repository;

import com.trohub.backend.modal.BankInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankInfoRepository extends JpaRepository<BankInfo, Long> {
    Optional<BankInfo> findTopByOrderByIdAsc();
}

