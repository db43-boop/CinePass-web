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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuentas_maestras",
        indexes = @Index(name = "idx_cuentas_plataforma_estado",
                columnList = "id_plataforma, estado"))
@Getter
@Setter
@NoArgsConstructor
public class CuentaMaestra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta_maestra", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plataforma", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cuentas_plataformas"))
    private Plataforma plataforma;

    @Column(name = "correo_cuenta", nullable = false, length = 150)
    private String correoCuenta;

    @Column(name = "password_cuenta", nullable = false, length = 255)
    private String passwordCuenta;

    @Column(name = "tipo_plan", length = 80)
    private String tipoPlan;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 8)
    private EstadoCuenta estado = EstadoCuenta.ACTIVA;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private Instant fechaRegistro;

    @OneToMany(mappedBy = "cuentaMaestra")
    private List<Perfil> perfiles = new ArrayList<>();

    public enum EstadoCuenta {
        ACTIVA,
        INACTIVA,
        VENCIDA
    }
}
