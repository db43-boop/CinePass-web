package com.cinepass.app.repository;

import com.cinepass.app.model.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Long usuarioId);
    Optional<Pedido> findByIdAndUsuarioId(Long id, Long usuarioId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> buscarParaActualizar(@Param("id") Long id);

    @Modifying
    @Query(value = """
            UPDATE pedidos pe
            JOIN perfiles p ON p.id_perfil = pe.id_perfil
            SET pe.estado = 'VENCIDA', p.estado = 'DISPONIBLE', p.fecha_reserva = NULL
            WHERE pe.id_perfil = :perfilId
              AND pe.estado = 'RESERVADA'
              AND pe.fecha_limite_reserva <= CURRENT_TIMESTAMP
              AND p.estado = 'RESERVADO'
            """, nativeQuery = true)
    int liberarReservaVencida(@Param("perfilId") Long perfilId);
}
