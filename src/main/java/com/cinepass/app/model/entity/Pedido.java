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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_pedidos_codigo", columnNames = "codigo_pedido"),
        indexes = {
                @Index(name = "idx_pedidos_usuario_fecha",
                        columnList = "id_usuario, fecha_pedido"),
                @Index(name = "idx_pedidos_perfil_estado",
                        columnList = "id_perfil, estado"),
                @Index(name = "idx_pedidos_estado_fecha",
                        columnList = "estado, fecha_pedido")
        })
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pedidos_usuarios"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pedidos_perfiles"))
    private Perfil perfil;

    @Column(name = "codigo_pedido", nullable = false, length = 40)
    private String codigoPedido;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 16)
    private EstadoPedido estado = EstadoPedido.RESERVADA;

    @Column(name = "fecha_pedido", insertable = false, updatable = false)
    private Instant fechaPedido;

    @Column(name = "fecha_limite_reserva")
    private LocalDateTime fechaLimiteReserva;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private Instant fechaActualizacion;

    @OneToMany(mappedBy = "pedido")
    private List<Transaccion> transacciones = new ArrayList<>();

    public enum EstadoPedido {
        RESERVADA,
        EN_VALIDACION,
        ACTIVA,
        VENCIDA,
        RECHAZADA,
        CANCELADA
    }
}
