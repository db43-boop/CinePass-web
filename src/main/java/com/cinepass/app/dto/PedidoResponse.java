package com.cinepass.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Información de un pedido para el panel del cliente. */
public record PedidoResponse(Long id, String codigo, Long perfilId,
                             String plataforma, String plan, BigDecimal monto,
                             String estado, LocalDateTime fechaLimiteReserva) {}
