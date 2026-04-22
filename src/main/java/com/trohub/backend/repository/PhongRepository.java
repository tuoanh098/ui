package com.trohub.backend.repository;

import com.trohub.backend.modal.Phong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Collection;

@Repository
public interface PhongRepository extends JpaRepository<Phong, Long> {
    Optional<Phong> findByMaPhong(String maPhong);
    long countByToaNhaId(Long toaNhaId);
    long countByToaNhaIdAndTrangThai(Long toaNhaId, String trangThai);
    java.util.List<Phong> findAllByToaNhaId(Long toaNhaId);
    java.util.List<Phong> findAllByToaNhaIdIn(Collection<Long> toaNhaIds);
}

