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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vouchers",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_vouchers_transaccion", columnNames = "id_transaccion"),
        indexes = @Index(name = "idx_vouchers_estado_fecha",
                columnList = "estado, fecha_registro"))
@Getter
@Setter
@NoArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_voucher", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_transaccion", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vouchers_transacciones"))
    private Transaccion transaccion;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 1000)
    private String rutaArchivo;

    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String tipoMime;

    @Column(name = "tamano_bytes", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long tamanoBytes;

    @Column(name = "sha256", length = 64)
    private String sha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoVoucher estado = EstadoVoucher.PENDIENTE;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private Instant fechaRegistro;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private Instant fechaActualizacion;

    @OneToMany(mappedBy = "voucher")
    private List<AuditoriaVoucher> auditorias = new ArrayList<>();

    public enum EstadoVoucher {
        PENDIENTE,
        APROBADO,
        RECHAZADO,
        OBSERVADO
    }
}
