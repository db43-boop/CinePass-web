package com.cinepass.app.controller;

import com.cinepass.app.dto.CuentaMaestraRequest;
import com.cinepass.app.model.entity.Usuario;
import com.cinepass.app.repository.CuentaMaestraRepository;
import com.cinepass.app.repository.PlataformaRepository;
import com.cinepass.app.repository.UsuarioRepository;
import com.cinepass.app.service.CuentaMaestraService;
import com.cinepass.app.service.PerfilService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
public class AdminInventarioController {
    private final CuentaMaestraService cuentasService;
    private final PerfilService perfilesService;
    private final CuentaMaestraRepository cuentas;
    private final PlataformaRepository plataformas;
    private final UsuarioRepository usuarios;

    public AdminInventarioController(CuentaMaestraService cuentasService, PerfilService perfilesService,
                                     CuentaMaestraRepository cuentas, PlataformaRepository plataformas,
                                     UsuarioRepository usuarios) {
        this.cuentasService = cuentasService; this.perfilesService = perfilesService;
        this.cuentas = cuentas; this.plataformas = plataformas; this.usuarios = usuarios;
    }

    private Long admin(HttpSession session) {
        Long id = (Long) session.getAttribute("adminLogged");
        if (id == null) return null;
        Usuario u = usuarios.findById(id).orElse(null);
        return u != null && u.getEstado() == Usuario.EstadoUsuario.ACTIVO
                && "ADMINISTRADOR".equals(u.getRol().getNombre()) ? id : null;
    }

    @GetMapping("/admin/inventario")
    public String inventario(HttpSession session, Model model) {
        if (admin(session) == null) return "redirect:/admin/login";
        model.addAttribute("plataformas", plataformas.findAll());
        model.addAttribute("cuentas", cuentas.findAll());
        return "admin-inventario";
    }

    @PostMapping("/admin/cuentas")
    public String crearCuenta(HttpSession session, @RequestParam Long plataformaId, @RequestParam String correo,
                              @RequestParam String password, @RequestParam(required = false) String tipoPlan,
                              @RequestParam BigDecimal costo, RedirectAttributes ra) {
        Long id = admin(session);
        if (id == null) return "redirect:/admin/login";
        try {
            cuentasService.crear(id, new CuentaMaestraRequest(plataformaId, correo, password, tipoPlan, null, null, costo, null));
            ra.addFlashAttribute("mensaje", "Cuenta creada.");
        } catch (IllegalArgumentException | org.springframework.dao.DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "No se pudo crear la cuenta. Revisa los datos y la contraseña.");
        }
        return "redirect:/admin/inventario";
    }

    @PostMapping("/admin/perfiles")
    public String crearPerfil(HttpSession session, @RequestParam Long cuentaId, @RequestParam String nombre,
                              @RequestParam(required = false) String pin, @RequestParam BigDecimal precio,
                              RedirectAttributes ra) {
        Long id = admin(session);
        if (id == null) return "redirect:/admin/login";
        try {
            perfilesService.crear(id, cuentaId, nombre, pin, precio);
            ra.addFlashAttribute("mensaje", "Perfil creado.");
        } catch (IllegalArgumentException | org.springframework.dao.DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "No se pudo crear el perfil. Revisa los datos y el nombre.");
        }
        return "redirect:/admin/inventario";
    }
}
