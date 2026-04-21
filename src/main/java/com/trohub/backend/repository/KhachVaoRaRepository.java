package com.trohub.backend.repository;

import com.trohub.backend.modal.KhachVaoRa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachVaoRaRepository extends JpaRepository<KhachVaoRa, Long> {
}

