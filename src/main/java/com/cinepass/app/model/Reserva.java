package com.cinepass.app.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/** Pedido del cliente. Estados: RESERVADA, EN_VALIDACION, ACTIVA, VENCIDA. */
@Data
@Builder
public class Reserva {
    private Long id;
    private Long perfilId;
    private String plataforma;
    private String slug;
    private String tipoPlan;
    private Double precio;
    private String estado;
    private Instant expiraEn;      // solo RESERVADA
    private String accesoCorreo;   // solo ACTIVA
    private String accesoPerfil;
    private String accesoPin;
    private String renuevaEl;
}
