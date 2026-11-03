package com.cinepass.app.controller;

import com.cinepass.app.dto.RegistroUsuarioRequest;
import com.cinepass.app.service.AutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public String login() { return "cliente-login"; }

    @PostMapping("/login")
    public String ingresar(@RequestParam String correo, @RequestParam String password,
                           HttpServletRequest request, Model model) {
        try {
            var usuario = autenticacion.autenticar(correo, password);
            HttpSession anterior = request.getSession(false);
            if (anterior != null) anterior.invalidate();
            HttpSession sesion = request.getSession(true);
            sesion.setAttribute("usuarioId", usuario.id());
            return "redirect:/cliente";
        } catch (SecurityException ex) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            return "cliente-login";
        }
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

    @PostMapping("/salir")
    public String salir(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) sesion.invalidate();
        return "redirect:/";
    }
}
