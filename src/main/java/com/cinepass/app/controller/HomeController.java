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
                              Model model) {
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

    @PostMapping("/checkout/{id}/comprobante")
    public String enviarComprobante(@PathVariable Long id,
                                    @RequestParam String metodo,
                                    @RequestParam String operacion,
                                    @RequestParam String titular,
                                    @RequestParam("comprobante") MultipartFile comprobante,
                                    RedirectAttributes ra) {
        Instant expira = reservasActivas.get(id);
        if (expira == null || Instant.now().isAfter(expira) || comprobante.isEmpty()) {
            return "redirect:/checkout/" + id; // la página mostrará el estado vencido
        }
        // TODO: guardar el archivo, registrar el pago (metodo, operacion, titular) y marcar EN_VALIDACION.
        reservasActivas.remove(id);
        ra.addFlashAttribute("mensaje", "Recibimos tu comprobante. Lo validamos y te avisamos en este panel.");
        return "redirect:/cliente";
    }

    @GetMapping("/admin")
    public String panelAdmin(HttpSession session, Model model) {
        if (session.getAttribute("adminLogged") == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("pendientes", 3);
        model.addAttribute("aprobadosHoy", 7);
        model.addAttribute("totalMes", 42);
        return "admin";
    }

    @GetMapping("/admin/login")
    public String loginForm(HttpSession session, Model model) {
        if (session.getAttribute("adminLogged") != null) {
            return "redirect:/admin";
        }
        return "login";
    }

    @PostMapping("/admin/login")
    public String loginSubmit(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            session.setAttribute("adminLogged", username);
            return "redirect:/admin";
        }
        model.addAttribute("error", "Usuario o contraseña incorrectos. Inténtalo de nuevo.");
        return "login";
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    @GetMapping("/cliente")
    public String panelCliente(Model model) {
        long ahora = System.currentTimeMillis();
        List<Reserva> reservas = List.of(
            Reserva.builder().id(101L).perfilId(1L).plataforma("Netflix").slug("netflix")
                .tipoPlan("Estándar - 1 Perfil (4K)").precio(12.0).estado("RESERVADA")
                .expiraEn(Instant.ofEpochMilli(ahora + 11 * 60_000L + 20_000L)).build(),
            Reserva.builder().id(102L).perfilId(2L).plataforma("Max (HBO)").slug("max")
                .tipoPlan("Plan Mes Completo").precio(10.0).estado("EN_VALIDACION").build(),
            Reserva.builder().id(103L).perfilId(3L).plataforma("Disney+").slug("disney")
                .tipoPlan("Familiar - 1 Dispositivo").precio(11.0).estado("ACTIVA")
                .accesoCorreo("cuenta.disney07@cinepass.pe").accesoPerfil("Perfil 3").accesoPin("4821")
                .renuevaEl("14 nov 2026").build(),
            Reserva.builder().id(104L).perfilId(1L).plataforma("Netflix").slug("netflix")
                .tipoPlan("Estándar - 1 Perfil (4K)").precio(12.0).estado("VENCIDA").build()
        );
        model.addAttribute("reservas", reservas);
        model.addAttribute("activas", reservas.stream().filter(r -> r.getEstado().equals("ACTIVA")).count());
        model.addAttribute("pendientes", reservas.stream()
                .filter(r -> r.getEstado().equals("RESERVADA") || r.getEstado().equals("EN_VALIDACION")).count());
        model.addAttribute("ahoraMs", ahora);
        return "cliente";
    }

    private List<Perfil> catalogo() {
        return List.of(
            perfil(1L, "Netflix", "netflix", "Estándar - 1 Perfil (4K)", 12.00,
                "https://www.mercadonegro.pe/wp-content/uploads/2023/12/netflix-2.png"),
            perfil(2L, "Max (HBO)", "max", "Plan Mes Completo", 10.00,
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQl93CG_oNImmzxYnJ2DopAmS2CIEQEQp9HYkI-44OHGaLrc2QqJnmFtVNH&s=10"),
            perfil(3L, "Disney+", "disney", "Familiar - 1 Dispositivo", 11.00,
                "https://images.unsplash.com/photo-1596727147705-61a532a659bd?w=600&h=400&fit=crop"));
    }

    private Perfil perfil(Long id, String plataforma, String slug, String plan, double precio, String imagenUrl) {
        Perfil p = new Perfil();
        p.setId(id); p.setPlataforma(plataforma); p.setSlug(slug);
        p.setTipoPlan(plan); p.setPrecio(precio); p.setEstado("DISPONIBLE");
        p.setImagenUrl(imagenUrl);
        return p;
    }
}
