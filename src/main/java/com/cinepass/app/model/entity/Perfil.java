package com.cinepass.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "perfiles",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_perfil_cuenta_nombre",
                columnNames = {"id_cuenta_maestra", "nombre_perfil"}),
        indexes = {
                @Index(name = "idx_perfiles_estado", columnList = "estado"),
                @Index(name = "idx_perfiles_cuenta_estado",
                        columnList = "id_cuenta_maestra, estado")
        })
@Getter
@Setter
@NoArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta_maestra", nullable = false,
            foreignKey = @ForeignKey(name = "fk_perfiles_cuentas"))
    private CuentaMaestra cuentaMaestra;

    @Column(name = "nombre_perfil", nullable = false, length = 80)
    private String nombrePerfil;

    @Column(name = "pin_acceso", length = 100)
    private String pinAcceso;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoPerfil estado = EstadoPerfil.DISPONIBLE;

    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private Instant fechaRegistro;

    public enum EstadoPerfil {
        DISPONIBLE,
        RESERVADO,
        VENDIDO
    }
}
