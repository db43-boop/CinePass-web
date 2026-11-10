package com.cinepass.app.controller;

import com.cinepass.app.model.Perfil;
import com.cinepass.app.model.Reserva;
import com.cinepass.app.security.CinePassUserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Controller
public class HomeController {

    private final com.cinepass.app.service.CatalogoService catalogoService;
    private final com.cinepass.app.service.PedidoService pedidoService;
    private final com.cinepass.app.service.ReservaService reservaService;
    private final com.cinepass.app.service.ComprobanteService comprobanteService;
    private final com.cinepass.app.service.ValidacionPagoService validacionPagoService;

    public HomeController(com.cinepass.app.service.CatalogoService catalogoService,
                          com.cinepass.app.service.PedidoService pedidoService,
                          com.cinepass.app.service.ReservaService reservaService,
                          com.cinepass.app.service.ComprobanteService comprobanteService,
                          com.cinepass.app.service.ValidacionPagoService validacionPagoService) {
        this.catalogoService = catalogoService;
        this.pedidoService = pedidoService;
        this.reservaService = reservaService;
        this.comprobanteService = comprobanteService;
        this.validacionPagoService = validacionPagoService;
    }

    private static final int RESERVA_SEG = 15 * 60;

    @GetMapping("/")
    public String verCatalogo(Model model) {
        model.addAttribute("perfiles", catalogo());
        model.addAttribute("plataformas", catalogoService.plataformasDisponibles());
        return "index";
    }

    @PostMapping("/reservas/{id}")
    public String crearReserva(@PathVariable Long id,
                               @AuthenticationPrincipal CinePassUserDetails usuario,
                               RedirectAttributes ra) {
        try {
            var pedido = reservaService.reservar(usuario.getId(), id);
            return "redirect:/pedidos/" + pedido.getId() + "/checkout";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/pedidos/{id}/checkout")
    public String checkoutPedido(@PathVariable Long id,
                                 @AuthenticationPrincipal CinePassUserDetails usuario,
                                 Model model) {
        var pedido = pedidoService.listar(usuario.getId()).stream()
                .filter(p -> p.id().equals(id)).findFirst().orElse(null);
        if (pedido == null || !"RESERVADA".equals(pedido.estado()) || pedido.fechaLimiteReserva() == null)
            return "redirect:/cliente";
        Perfil perfil = new Perfil();
        perfil.setId(pedido.perfilId());
        perfil.setPlataforma(pedido.plataforma());
        perfil.setTipoPlan(pedido.plan());
        perfil.setPrecio(pedido.monto().doubleValue());
        model.addAttribute("perfil", perfil);
        model.addAttribute("pedidoId", pedido.id());
        model.addAttribute("expiraMs", pedido.fechaLimiteReserva().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        model.addAttribute("ahoraMs", System.currentTimeMillis());
        model.addAttribute("totalSeg", RESERVA_SEG);
        return "checkout";
    }

    @PostMapping("/pedidos/{id}/comprobante")
    public String registrarComprobante(@PathVariable Long id, @RequestParam String metodo,
                                       @RequestParam String operacion, @RequestParam String titular,
                                       @RequestParam("comprobante") MultipartFile archivo,
                                       @AuthenticationPrincipal CinePassUserDetails usuario,
                                       RedirectAttributes ra) throws java.io.IOException {
        try {
            comprobanteService.registrar(usuario.getId(), id,
                    new com.cinepass.app.dto.ComprobanteRequest(metodo, operacion, titular, archivo));
            ra.addFlashAttribute("mensaje", "Recibimos tu comprobante y está pendiente de validación.");
            return "redirect:/cliente";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/pedidos/" + id + "/checkout";
        }
    }

    @GetMapping("/admin")
    public String panelAdmin(@AuthenticationPrincipal CinePassUserDetails administrador, Model model) {
        var comprobantes = validacionPagoService.pendientes(administrador.getId());
        model.addAttribute("comprobantes", comprobantes);
        model.addAttribute("pendientes", comprobantes.size());
        return "admin-pendientes";
    }

    @PostMapping("/admin/comprobantes/{id}/resolver")
    public String resolverComprobante(@PathVariable Long id, @RequestParam boolean aprobar,
                                      @AuthenticationPrincipal CinePassUserDetails administrador,
                                      RedirectAttributes ra) {
        try {
            validacionPagoService.resolver(administrador.getId(), id, aprobar);
            ra.addFlashAttribute("mensaje", aprobar ? "Pago aprobado." : "Pago rechazado.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/comprobantes/{id}/archivo")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> descargarComprobante(
            @PathVariable Long id,
            @AuthenticationPrincipal CinePassUserDetails administrador) throws java.io.IOException {
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"comprobante-" + id + ".pdf\"")
                .header("X-Content-Type-Options", "nosniff")
                .header("Cache-Control", "no-store")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(validacionPagoService.leerComprobante(administrador.getId(), id));
    }

    @GetMapping("/cliente")
    public String panelCliente(Model model,
                               @AuthenticationPrincipal CinePassUserDetails usuario,
                               jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        Long usuarioId = usuario.getId();
        long ahora = System.currentTimeMillis();
        List<Reserva> reservas = pedidoService.listar(usuarioId).stream().map(p -> {
            String nombre = p.plataforma().toLowerCase(java.util.Locale.ROOT);
            var builder = Reserva.builder().id(p.id()).perfilId(p.perfilId()).plataforma(p.plataforma())
                    .slug(nombre.contains("netflix") ? "netflix" : nombre.contains("disney") ? "disney" : "max")
                    .tipoPlan(p.plan()).precio(p.monto().doubleValue()).estado(p.estado());
            if (p.fechaLimiteReserva() != null)
                builder.expiraEn(p.fechaLimiteReserva().atZone(java.time.ZoneId.systemDefault()).toInstant());
            if ("ACTIVA".equals(p.estado())) {
                var credenciales = pedidoService.credenciales(usuarioId, p.id());
                builder.accesoCorreo(credenciales.correo()).accesoPassword(credenciales.password()).accesoPerfil(credenciales.perfil()).accesoPin(credenciales.pin());
            }
            return builder.build();
        }).toList();
        model.addAttribute("reservas", reservas);
        model.addAttribute("activas", reservas.stream().filter(r -> r.getEstado().equals("ACTIVA")).count());
        model.addAttribute("pendientes", reservas.stream()
                .filter(r -> r.getEstado().equals("RESERVADA") || r.getEstado().equals("EN_VALIDACION")).count());
        model.addAttribute("ahoraMs", ahora);
        return "cliente";
    }

    private List<Perfil> catalogo() {
        return catalogoService.disponibles();
    }
}
