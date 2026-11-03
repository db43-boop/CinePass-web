package com.cinepass.app.model;

import lombok.Data;

@Data
public class Perfil {
    private Long id;
    private String plataforma;
    private String slug;      // netflix | max | disney -> color de marca en el CSS
    private String tipoPlan;
    private Double precio;
    private String estado;
    private String imagenUrl; // URL de imagen referencial de la plataforma
}
