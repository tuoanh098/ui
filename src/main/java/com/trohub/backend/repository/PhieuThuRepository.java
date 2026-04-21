package com.trohub.backend.repository;

import com.trohub.backend.modal.billing.PhieuThu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PhieuThuRepository extends JpaRepository<PhieuThu, Long> {

    @Query("select coalesce(sum(p.amountPaid),0) from PhieuThu p where p.hoaDonId = :hoaDonId")
    BigDecimal sumPaidByHoaDonId(@Param("hoaDonId") Long hoaDonId);

    PhieuThu findByTransactionId(String transactionId);
    List<PhieuThu> findByHoaDonId(Long hoaDonId);
}

