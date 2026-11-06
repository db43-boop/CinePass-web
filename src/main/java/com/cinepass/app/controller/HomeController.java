package com.cinepass.app.controller;

import com.cinepass.app.model.Perfil;
import com.cinepass.app.model.Reserva;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class HomeController {

    private final com.cinepass.app.service.CatalogoService catalogoService;
    private final com.cinepass.app.service.AutenticacionService autenticacionService;
    private final com.cinepass.app.service.PedidoService pedidoService;
    private final com.cinepass.app.service.ReservaService reservaService;
    private final com.cinepass.app.service.ComprobanteService comprobanteService;
    private final com.cinepass.app.service.ValidacionPagoService validacionPagoService;

    public HomeController(com.cinepass.app.service.CatalogoService catalogoService,
                          com.cinepass.app.service.AutenticacionService autenticacionService,
                          com.cinepass.app.service.PedidoService pedidoService,
                          com.cinepass.app.service.ReservaService reservaService,
                          com.cinepass.app.service.ComprobanteService comprobanteService,
                          com.cinepass.app.service.ValidacionPagoService validacionPagoService) {
        this.catalogoService = catalogoService;
        this.autenticacionService = autenticacionService;
        this.pedidoService = pedidoService;
        this.reservaService = reservaService;
        this.comprobanteService = comprobanteService;
        this.validacionPagoService = validacionPagoService;
    }

    private static final int RESERVA_SEG = 15 * 60;

    // Simulación en memoria. En producción: tabla "reserva" con expira_en como fuente de verdad.
    private final Map<Long, Instant> reservasActivas = new ConcurrentHashMap<>();

    @GetMapping("/")
    public String verCatalogo(Model model) {
        model.addAttribute("perfiles", catalogo());
        return "index";
    }

    /**
     * El vencimiento vive en el servidor: si el cliente recarga la página, el tiempo no se reinicia.
     * ?demo=20 crea una reserva de 20 s para probar el diseño de vencimiento.
     * ?nueva=1 genera una reserva nueva (botón "Reservar de nuevo").
     */
    @GetMapping("/checkout/{id}")
    public String verCheckout(@PathVariable Long id,
                              @RequestParam(required = false) Integer demo,
                              @RequestParam(required = false) String nueva,
                              Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";
        model.addAttribute("error", "Selecciona un perfil disponible para crear una reserva.");
        return "redirect:/";
    }

    @PostMapping("/reservas/{id}")
    public String crearReserva(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";
        try {
            var pedido = reservaService.reservar(usuarioId, id);
            return "redirect:/pedidos/" + pedido.getId() + "/checkout";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/pedidos/{id}/checkout")
    public String checkoutPedido(@PathVariable Long id, HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";
        var pedido = pedidoService.listar(usuarioId).stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
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

    /* Legacy simulated checkout is no longer reachable. */
    private String checkoutSimulado(Long id, Integer demo, String nueva, Model model) {
        Perfil perfil = catalogo().stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
        if (perfil == null) return "redirect:/";

        int total = (demo != null && demo > 0) ? demo : RESERVA_SEG;
        Instant expira = (demo != null || nueva != null)
                ? reservasActivas.merge(id, Instant.now().plusSeconds(total), (a, b) -> b)
                : reservasActivas.computeIfAbsent(id, k -> Instant.now().plusSeconds(RESERVA_SEG));

        model.addAttribute("perfil", perfil);
        model.addAttribute("expiraMs", expira.toEpochMilli());
        model.addAttribute("ahoraMs", System.currentTimeMillis());
        model.addAttribute("totalSeg", total);
        return "checkout";
    }

    @PostMapping("/pedidos/{id}/comprobante")
    public String registrarComprobante(@PathVariable Long id, @RequestParam String metodo,
                                       @RequestParam String operacion, @RequestParam String titular,
                                       @RequestParam("comprobante") MultipartFile archivo,
                                       HttpSession session, RedirectAttributes ra) throws java.io.IOException {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";
        try {
            comprobanteService.registrar(usuarioId, id,
                    new com.cinepass.app.dto.ComprobanteRequest(metodo, operacion, titular, archivo));
            ra.addFlashAttribute("mensaje", "Recibimos tu comprobante y está pendiente de validación.");
            return "redirect:/cliente";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/pedidos/" + id + "/checkout";
        }
    }


    @GetMapping("/admin")
    public String panelAdmin(HttpSession session, Model model) {
        if (session.getAttribute("adminLogged") == null) {
            return "redirect:/admin/login";
        }
        Long adminId = (Long) session.getAttribute("adminLogged");
        var comprobantes = validacionPagoService.pendientes(adminId);
        model.addAttribute("comprobantes", comprobantes);
        model.addAttribute("pendientes", comprobantes.size());
        return "admin-pendientes";
    }

    @PostMapping("/admin/comprobantes/{id}/resolver")
    public String resolverComprobante(@PathVariable Long id, @RequestParam boolean aprobar,
                                      HttpSession session, RedirectAttributes ra) {
        Long adminId = (Long) session.getAttribute("adminLogged");
        if (adminId == null) return "redirect:/admin/login";
        try {
            validacionPagoService.resolver(adminId, id, aprobar);
            ra.addFlashAttribute("mensaje", aprobar ? "Pago aprobado." : "Pago rechazado.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/comprobantes/{id}/archivo")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> descargarComprobante(@PathVariable Long id, HttpSession session) throws java.io.IOException {
        Long adminId = (Long) session.getAttribute("adminLogged");
        if (adminId == null) return org.springframework.http.ResponseEntity.status(401).build();
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"comprobante\"")
                .header("X-Content-Type-Options", "nosniff")
                .header("Cache-Control", "no-store")
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(validacionPagoService.leerComprobante(adminId, id));
    }

    @GetMapping("/admin/login")
    public String loginForm(HttpSession session, Model model) {
        if (session.getAttribute("adminLogged") != null) {
            return "redirect:/admin";
        }
        return "login";
    }

    @PostMapping("/admin/login")
    public String loginSubmit(@RequestParam String correo,
                              @RequestParam String password,
                              HttpSession session,
                              jakarta.servlet.http.HttpServletRequest request,
                              Model model) {
        try {
            var usuario = autenticacionService.autenticar(correo, password);
            if ("ADMINISTRADOR".equals(usuario.rol())) {
                request.changeSessionId();
                session.setAttribute("adminLogged", usuario.id());
                return "redirect:/admin";
            }
        } catch (SecurityException ignored) { }
        model.addAttribute("error", "Usuario o contraseña incorrectos. Inténtalo de nuevo.");
        return "login";
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    @GetMapping("/cliente")
    public String panelCliente(Model model, HttpSession session, jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";
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

    private Perfil perfil(Long id, String plataforma, String slug, String plan, double precio, String imagenUrl) {
        Perfil p = new Perfil();
        p.setId(id); p.setPlataforma(plataforma); p.setSlug(slug);
        p.setTipoPlan(plan); p.setPrecio(precio); p.setEstado("DISPONIBLE");
        p.setImagenUrl(imagenUrl);
        return p;
    }
}
