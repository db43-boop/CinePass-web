package com.cinepass.app.dto;

/** Datos que solo deben entregarse al propietario de un pedido activo. */
public record CredencialesResponse(String correo, String password,
                                   String perfil, String pin) {}
