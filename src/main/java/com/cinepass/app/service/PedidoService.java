package com.cinepass.app.service;

import com.cinepass.app.dto.CredencialesResponse;
import com.cinepass.app.dto.PedidoResponse;
import com.cinepass.app.model.entity.Pedido;
import com.cinepass.app.model.entity.Usuario;
import com.cinepass.app.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    public PedidoService(PedidoRepository pedidos) { this.pedidos = pedidos; }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(long usuarioId) {
        return pedidos.findByUsuarioIdOrderByFechaPedidoDesc(usuarioId).stream().map(p -> {
            var perfil = p.getPerfil();
            var cuenta = perfil.getCuentaMaestra();
            return new PedidoResponse(p.getId(), p.getCodigoPedido(), perfil.getId(),
                    cuenta.getPlataforma().getNombre(), cuenta.getTipoPlan(), p.getMontoTotal(),
                    p.getEstado().name(), p.getFechaLimiteReserva());
        }).toList();
    }

    // El identificador debe proceder exclusivamente de la sesión autenticada.
    @Transactional(readOnly = true)
    public CredencialesResponse credenciales(long usuarioId, long pedidoId) {
        Pedido pedido = pedidos.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        if (pedido.getUsuario().getEstado() != Usuario.EstadoUsuario.ACTIVO
                || pedido.getEstado() != Pedido.EstadoPedido.ACTIVA)
            throw new SecurityException("Acceso no disponible");
        var perfil = pedido.getPerfil();
        var cuenta = perfil.getCuentaMaestra();
        return new CredencialesResponse(cuenta.getCorreoCuenta(), cuenta.getPasswordCuenta(),
                perfil.getNombrePerfil(), perfil.getPinAcceso());
    }
}
