package com.cinepass.app.controller;

import com.cinepass.app.dto.CuentaMaestraRequest;
import com.cinepass.app.security.CinePassUserDetails;
import com.cinepass.app.repository.CuentaMaestraRepository;
import com.cinepass.app.repository.PlataformaRepository;
import com.cinepass.app.service.CuentaMaestraService;
import com.cinepass.app.service.PerfilService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    public AdminInventarioController(CuentaMaestraService cuentasService, PerfilService perfilesService,
                                     CuentaMaestraRepository cuentas, PlataformaRepository plataformas) {
        this.cuentasService = cuentasService; this.perfilesService = perfilesService;
        this.cuentas = cuentas; this.plataformas = plataformas;
    }

    @GetMapping("/admin/inventario")
    public String inventario(Model model) {
        model.addAttribute("plataformas", plataformas.findAll());
        model.addAttribute("cuentas", cuentas.findAll());
        return "admin-inventario";
    }

    @PostMapping("/admin/cuentas")
    public String crearCuenta(@AuthenticationPrincipal CinePassUserDetails administrador,
                              @RequestParam Long plataformaId, @RequestParam String correo,
                              @RequestParam String password, @RequestParam(required = false) String tipoPlan,
                              @RequestParam BigDecimal costo, RedirectAttributes ra) {
        try {
            cuentasService.crear(administrador.getId(), new CuentaMaestraRequest(
                    plataformaId, correo, password, tipoPlan, null, null, costo, null));
            ra.addFlashAttribute("mensaje", "Cuenta creada.");
        } catch (IllegalArgumentException | org.springframework.dao.DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "No se pudo crear la cuenta. Revisa los datos y la contraseña.");
        }
        return "redirect:/admin/inventario";
    }

    @PostMapping("/admin/perfiles")
    public String crearPerfil(@AuthenticationPrincipal CinePassUserDetails administrador,
                              @RequestParam Long cuentaId, @RequestParam String nombre,
                              @RequestParam(required = false) String pin, @RequestParam BigDecimal precio,
                              RedirectAttributes ra) {
        try {
            perfilesService.crear(administrador.getId(), cuentaId, nombre, pin, precio);
            ra.addFlashAttribute("mensaje", "Perfil creado.");
        } catch (IllegalArgumentException | org.springframework.dao.DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "No se pudo crear el perfil. Revisa los datos y el nombre.");
        }
        return "redirect:/admin/inventario";
    }
}
