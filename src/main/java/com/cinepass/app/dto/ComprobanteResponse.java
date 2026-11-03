package com.cinepass.app.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Datos del comprobante para la revisión administrativa. */
public record ComprobanteResponse(Long id, Long pedidoId, String cliente,
                                 String plataforma, String metodoPago,
                                 String numeroOperacion, BigDecimal monto,
                                 String estado, Instant fechaRegistro) {}
