package com.cinepass.app.model;

import lombok.Data;

@Data
public class Perfil {
    private Long id;
    private String plataforma;
    private String tipoPlan;
    private Double precio;
    private String estado;
}
