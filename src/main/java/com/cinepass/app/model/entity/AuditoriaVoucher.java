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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "auditoria_vouchers", indexes = {
        @Index(name = "idx_auditoria_voucher_fecha",
                columnList = "id_voucher, fecha_evento"),
        @Index(name = "idx_auditoria_usuario_fecha",
                columnList = "id_usuario_actor, fecha_evento")
})
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_voucher", nullable = false,
            foreignKey = @ForeignKey(name = "fk_auditoria_vouchers_voucher"))
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_actor",
            foreignKey = @ForeignKey(name = "fk_auditoria_vouchers_usuario"))
    private Usuario usuarioActor;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 10)
    private AccionAuditoria accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 10)
    private Voucher.EstadoVoucher estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 10)
    private Voucher.EstadoVoucher estadoNuevo;

    @Column(name = "detalle", nullable = false, length = 500)
    private String detalle;

    @Column(name = "fecha_evento", insertable = false, updatable = false)
    private Instant fechaEvento;

    public enum AccionAuditoria {
        CREADO,
        ACTUALIZADO
    }
}
