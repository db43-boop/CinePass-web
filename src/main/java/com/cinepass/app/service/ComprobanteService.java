package com.cinepass.app.service;

import com.cinepass.app.dto.ComprobanteRequest;
import com.cinepass.app.model.entity.Pedido;
import com.cinepass.app.model.entity.Transaccion;
import com.cinepass.app.model.entity.Voucher;
import com.cinepass.app.repository.PedidoRepository;
import com.cinepass.app.repository.TransaccionRepository;
import com.cinepass.app.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class ComprobanteService {
    private final PedidoRepository pedidos;
    private final TransaccionRepository transacciones;
    private final VoucherRepository vouchers;
    private final ComprobantePdfConverter pdfConverter;

    public ComprobanteService(PedidoRepository pedidos, TransaccionRepository transacciones,
                              VoucherRepository vouchers, ComprobantePdfConverter pdfConverter) {
        this.pedidos = pedidos;
        this.transacciones = transacciones;
        this.vouchers = vouchers;
        this.pdfConverter = pdfConverter;
    }

    @Transactional
    public Long registrar(long usuarioId, long pedidoId, ComprobanteRequest datos) throws IOException {
        Pedido pedido = pedidos.buscarParaActualizar(pedidoId).orElse(null);
        if (pedido == null || !pedido.getUsuario().getId().equals(usuarioId))
            throw new IllegalArgumentException("Pedido no encontrado");
        if (pedido.getEstado() != Pedido.EstadoPedido.RESERVADA || pedido.getFechaLimiteReserva() == null
                || !pedido.getFechaLimiteReserva().isAfter(LocalDateTime.now()))
            throw new IllegalStateException("La reserva no está vigente");
        if (datos == null || datos.archivo() == null || datos.archivo().isEmpty()
                || datos.archivo().getSize() > 5 * 1024 * 1024 || datos.metodoPago() == null
                || datos.numeroOperacion() == null || datos.numeroOperacion().isBlank()
                || datos.numeroOperacion().length() > 100 || datos.titular() == null
                || datos.titular().isBlank() || datos.titular().length() > 150)
            throw new IllegalArgumentException("Datos del comprobante inválidos");
        String metodo = datos.metodoPago().trim().toUpperCase(Locale.ROOT);
        if (!metodo.equals("YAPE") && !metodo.equals("PLIN")) throw new IllegalArgumentException("Método inválido");
        String operacion = datos.numeroOperacion().trim();
        if (transacciones.existsByMetodoPagoAndNumeroOperacion(metodo, operacion))
            throw new IllegalArgumentException("La operación ya está registrada");
        byte[] archivoOriginal = datos.archivo().getBytes();
        String mime;
        if (archivoOriginal.length >= 5 && new String(archivoOriginal, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) mime = "application/pdf";
        else if (archivoOriginal.length >= 8 && archivoOriginal[0] == (byte) 0x89 && archivoOriginal[1] == 0x50 && archivoOriginal[2] == 0x4e && archivoOriginal[3] == 0x47 && archivoOriginal[4] == 0x0d && archivoOriginal[5] == 0x0a && archivoOriginal[6] == 0x1a && archivoOriginal[7] == 0x0a) mime = "image/png";
        else if (archivoOriginal.length >= 3 && archivoOriginal[0] == (byte) 0xff && archivoOriginal[1] == (byte) 0xd8 && archivoOriginal[2] == (byte) 0xff) mime = "image/jpeg";
        else throw new IllegalArgumentException("Solo se aceptan PDF, PNG o JPEG");
        byte[] archivo = pdfConverter.convertir(archivoOriginal, mime);
        mime = "application/pdf";
        Path carpeta = Path.of("uploads", "comprobantes").toAbsolutePath().normalize();
        Files.createDirectories(carpeta);
        Path destino = carpeta.resolve(UUID.randomUUID().toString());
        Files.write(destino, archivo, java.nio.file.StandardOpenOption.CREATE_NEW);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) try { Files.deleteIfExists(destino); } catch (IOException ignored) { }
            }
        });
        Transaccion transaccion = new Transaccion();
        transaccion.setPedido(pedido);
        transaccion.setMetodoPago(metodo);
        transaccion.setNumeroOperacion(operacion);
        transaccion.setTitular(datos.titular().trim());
        transaccion.setMonto(pedido.getMontoTotal());
        transaccion.setEstado(Transaccion.EstadoTransaccion.EN_VALIDACION);
        transacciones.save(transaccion);
        Voucher voucher = new Voucher();
        voucher.setTransaccion(transaccion);
        voucher.setNombreArchivo("comprobante");
        voucher.setRutaArchivo(destino.toString());
        voucher.setTipoMime(mime);
        voucher.setTamanoBytes((long) archivo.length);
        try { voucher.setSha256(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(archivo))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        vouchers.save(voucher);
        pedido.setEstado(Pedido.EstadoPedido.EN_VALIDACION);
        return voucher.getId();
    }
}
