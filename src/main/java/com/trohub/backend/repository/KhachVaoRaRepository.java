package com.trohub.backend.repository;

import com.trohub.backend.modal.KhachVaoRa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KhachVaoRaRepository extends JpaRepository<KhachVaoRa, Long> {
    List<KhachVaoRa> findAllByPhongIdIn(Collection<Long> roomIds);
}

