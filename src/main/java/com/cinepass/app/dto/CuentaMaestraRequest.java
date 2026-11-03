package com.cinepass.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuentaMaestraRequest(Long plataformaId, String correo, String password,
                                   String tipoPlan, LocalDate fechaAdquisicion,
                                   LocalDate fechaVencimiento, BigDecimal costo, String observaciones) {}
