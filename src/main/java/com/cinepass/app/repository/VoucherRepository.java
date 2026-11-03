package com.cinepass.app.repository;

import com.cinepass.app.model.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByEstadoOrderByFechaRegistroAsc(Voucher.EstadoVoucher estado);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Voucher v where v.id = :id")
    Optional<Voucher> buscarParaActualizar(@Param("id") Long id);
}
