package com.trohub.backend.repository;

import com.trohub.backend.modal.ToaNha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToaNhaRepository extends JpaRepository<ToaNha, Long> {
    List<ToaNha> findAllByChuTroId(Long chuTroId);
}

