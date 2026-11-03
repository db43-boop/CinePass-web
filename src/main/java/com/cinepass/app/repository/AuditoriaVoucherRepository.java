package com.cinepass.app.repository;

import com.cinepass.app.model.entity.AuditoriaVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaVoucherRepository extends JpaRepository<AuditoriaVoucher, Long> {
    List<AuditoriaVoucher> findByVoucherIdOrderByFechaEventoDesc(Long voucherId);
}
