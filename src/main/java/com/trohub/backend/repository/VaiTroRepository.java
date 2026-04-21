package com.trohub.backend.repository;

import com.trohub.backend.modal.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {
    Optional<VaiTro> findByName(String name);
}

