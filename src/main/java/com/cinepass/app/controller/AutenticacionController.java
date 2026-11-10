package com.cinepass.app.controller;

import com.cinepass.app.dto.RegistroUsuarioRequest;
import com.cinepass.app.security.CinePassUserDetails;
import com.cinepass.app.service.AutenticacionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AutenticacionController {
    private final AutenticacionService autenticacion;

    public AutenticacionController(AutenticacionService autenticacion) { this.autenticacion = autenticacion; }

    @GetMapping("/login")
    public String login(Authentication authentication,
                        @AuthenticationPrincipal CinePassUserDetails usuario) {
        if (authentication != null && authentication.isAuthenticated() && usuario != null) {
            return usuario.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMINISTRADOR"::equals)
                    ? "redirect:/admin" : "redirect:/cliente";
        }
        return "cliente-login";
    }

    @GetMapping("/registro")
    public String registro() { return "registro"; }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombres, @RequestParam String apellidos,
                            @RequestParam String correo, @RequestParam String password,
                            @RequestParam(required = false) String telefono, Model model) {
        try {
            autenticacion.registrar(new RegistroUsuarioRequest(nombres, apellidos, correo, password, telefono));
            return "redirect:/login?registrado";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "registro";
        }
    }

}
