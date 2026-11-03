package com.cinepass.app.repository;

import com.cinepass.app.model.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByPedidoIdOrderByFechaRegistroDesc(Long pedidoId);
    boolean existsByMetodoPagoAndNumeroOperacion(String metodoPago, String numeroOperacion);
}
