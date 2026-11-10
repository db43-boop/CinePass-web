package com.cinepass.app.service;

import com.cinepass.app.model.entity.CuentaMaestra;
import com.cinepass.app.model.entity.Pedido;
import com.cinepass.app.model.entity.Perfil;
import com.cinepass.app.model.entity.Plataforma;
import com.cinepass.app.model.entity.Usuario;
import com.cinepass.app.repository.PerfilRepository;
import com.cinepass.app.repository.PedidoRepository;
import com.cinepass.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReservaService {
    private final PerfilRepository perfiles;
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;

    public ReservaService(PerfilRepository perfiles, PedidoRepository pedidos, UsuarioRepository usuarios) {
        this.perfiles = perfiles;
        this.pedidos = pedidos;
        this.usuarios = usuarios;
    }

    @Transactional
    public Pedido reservar(long usuarioId, long perfilId) {
        Usuario usuario = usuarios.findById(usuarioId).orElse(null);
        pedidos.liberarReservaVencida(perfilId);
        Perfil perfil = perfiles.buscarParaActualizar(perfilId).orElse(null);
        if (usuario == null || usuario.getEstado() != Usuario.EstadoUsuario.ACTIVO || perfil == null)
            throw new IllegalArgumentException("Usuario o perfil no disponible");
        CuentaMaestra cuenta = perfil.getCuentaMaestra();
        if (perfil.getEstado() != Perfil.EstadoPerfil.DISPONIBLE
                || cuenta.getEstado() != CuentaMaestra.EstadoCuenta.ACTIVA
                || cuenta.getPlataforma().getEstado() != Plataforma.EstadoPlataforma.ACTIVA)
            throw new IllegalStateException("El perfil ya no está disponible");
        LocalDateTime ahora = LocalDateTime.now();
        perfil.setEstado(Perfil.EstadoPerfil.RESERVADO);
        perfil.setFechaReserva(ahora);
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setPerfil(perfil);
        pedido.setCodigoPedido(UUID.randomUUID().toString());
        pedido.setMontoTotal(perfil.getPrecioVenta());
        pedido.setFechaLimiteReserva(ahora.plusMinutes(15));
        return pedidos.save(pedido);
    }
}
