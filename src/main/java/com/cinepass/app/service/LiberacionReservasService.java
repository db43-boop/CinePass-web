package com.cinepass.app.service;

import jakarta.persistence.EntityManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiberacionReservasService {
    private final EntityManager entityManager;

    public LiberacionReservasService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Scheduled(fixedDelayString = "${cinepass.reservas.liberacion-ms:10000}")
    @Transactional
    public void liberarReservasVencidas() {
        entityManager.createNativeQuery("""
                UPDATE pedidos pe
                JOIN perfiles p ON p.id_perfil = pe.id_perfil
                SET pe.estado = 'VENCIDA', p.estado = 'DISPONIBLE', p.fecha_reserva = NULL
                WHERE pe.estado = 'RESERVADA'
                  AND pe.fecha_limite_reserva <= CURRENT_TIMESTAMP
                  AND p.estado = 'RESERVADO'
                """).executeUpdate();
    }
}
