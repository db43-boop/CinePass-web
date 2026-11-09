package com.cinepass.app.service;

import com.cinepass.app.model.entity.Pedido;
import com.cinepass.app.model.entity.Perfil;
import com.cinepass.app.model.entity.Transaccion;
import com.cinepass.app.model.entity.Usuario;
import com.cinepass.app.model.entity.Voucher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cinepass.app.dto.ComprobanteResponse;
import com.cinepass.app.repository.VoucherRepository;
import com.cinepass.app.repository.PedidoRepository;
import com.cinepass.app.repository.PerfilRepository;
import com.cinepass.app.repository.UsuarioRepository;
import java.util.List;

@Service
public class ValidacionPagoService {
    private final VoucherRepository vouchers;
    private final PedidoRepository pedidos;
    private final PerfilRepository perfiles;
    private final UsuarioRepository usuarios;
    private final ComprobantePdfConverter pdfConverter;

    public ValidacionPagoService(VoucherRepository vouchers, PedidoRepository pedidos,
                                PerfilRepository perfiles, UsuarioRepository usuarios,
                                ComprobantePdfConverter pdfConverter) {
        this.vouchers = vouchers;
        this.pedidos = pedidos;
        this.perfiles = perfiles;
        this.usuarios = usuarios;
        this.pdfConverter = pdfConverter;
    }

    @Transactional(readOnly = true)
    public List<ComprobanteResponse> pendientes(long administradorId) {
        validarAdministrador(administradorId);
        return vouchers.findByEstadoOrderByFechaRegistroAsc(Voucher.EstadoVoucher.PENDIENTE)
                .stream().map(v -> {
                    var t = v.getTransaccion();
                    var p = t.getPedido();
                    var u = p.getUsuario();
                    return new ComprobanteResponse(v.getId(), p.getId(),
                            u.getNombres() + " " + u.getApellidos(),
                            p.getPerfil().getCuentaMaestra().getPlataforma().getNombre(),
                            t.getMetodoPago(), t.getNumeroOperacion(), t.getMonto(),
                            v.getEstado().name(), v.getFechaRegistro());
                }).toList();
    }

    private void validarAdministrador(long administradorId) {
        Usuario actor = usuarios.findById(administradorId).orElse(null);
        if (actor == null || actor.getEstado() != Usuario.EstadoUsuario.ACTIVO
                || !"ADMINISTRADOR".equals(actor.getRol().getNombre()))
            throw new SecurityException("Se requiere un administrador activo");
    }

    @Transactional(readOnly = true)
    public byte[] leerComprobante(long administradorId, long voucherId) throws java.io.IOException {
        validarAdministrador(administradorId);
        Voucher voucher = vouchers.findById(voucherId).orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
        java.nio.file.Path carpeta = java.nio.file.Path.of("uploads", "comprobantes").toAbsolutePath().normalize();
        java.nio.file.Path archivo = java.nio.file.Path.of(voucher.getRutaArchivo()).toAbsolutePath().normalize();
        if (!archivo.getParent().equals(carpeta) || java.nio.file.Files.isSymbolicLink(archivo))
            throw new SecurityException("Ruta de comprobante inválida");
        return pdfConverter.convertir(java.nio.file.Files.readAllBytes(archivo), voucher.getTipoMime());
    }

    @Transactional
    public void resolver(long administradorId, long voucherId, boolean aprobar) {
        validarAdministrador(administradorId);
        Voucher voucher = vouchers.buscarParaActualizar(voucherId).orElse(null);
        if (voucher == null) throw new IllegalArgumentException("Comprobante no encontrado");
        Transaccion transaccion = voucher.getTransaccion();
        Pedido pedido = pedidos.buscarParaActualizar(transaccion.getPedido().getId()).orElseThrow();
        Perfil perfil = perfiles.buscarParaActualizar(pedido.getPerfil().getId()).orElseThrow();
        if (voucher.getEstado() != Voucher.EstadoVoucher.PENDIENTE
                || pedido.getEstado() != Pedido.EstadoPedido.EN_VALIDACION
                || perfil.getEstado() != Perfil.EstadoPerfil.RESERVADO) {
            throw new IllegalStateException("El comprobante no está pendiente de validación");
        }
        voucher.setEstado(aprobar ? Voucher.EstadoVoucher.APROBADO : Voucher.EstadoVoucher.RECHAZADO);
        transaccion.setEstado(aprobar ? Transaccion.EstadoTransaccion.APROBADA : Transaccion.EstadoTransaccion.RECHAZADA);
        pedido.setEstado(aprobar ? Pedido.EstadoPedido.ACTIVA : Pedido.EstadoPedido.RECHAZADA);
        perfil.setEstado(aprobar ? Perfil.EstadoPerfil.VENDIDO : Perfil.EstadoPerfil.DISPONIBLE);
        perfil.setFechaReserva(null);
    }
}
