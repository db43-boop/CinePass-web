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

@Entity
@Table(name = "transacciones",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_transaccion_metodo_operacion",
                columnNames = {"metodo_pago", "numero_operacion"}),
        indexes = {
                @Index(name = "idx_transacciones_pedido_fecha",
                        columnList = "id_pedido, fecha_registro"),
                @Index(name = "idx_transacciones_estado_fecha",
                        columnList = "estado, fecha_registro")
        })
@Getter
@Setter
@NoArgsConstructor
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transacciones_pedidos"))
    private Pedido pedido;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @Column(name = "numero_operacion", nullable = false, length = 100)
    private String numeroOperacion;

    @Column(name = "titular", nullable = false, length = 150)
    private String titular;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 13)
    private EstadoTransaccion estado = EstadoTransaccion.PENDIENTE;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private Instant fechaRegistro;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private Instant fechaActualizacion;

    public enum EstadoTransaccion {
        PENDIENTE,
        EN_VALIDACION,
        APROBADA,
        RECHAZADA,
        CANCELADA
    }
}
