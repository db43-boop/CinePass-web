package com.cinepass.app.dto;

public record RegistroUsuarioRequest(String nombres, String apellidos, String correo,
                                    String password, String telefono) {}
