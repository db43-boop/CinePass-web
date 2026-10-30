package com.cinepass.app.controller;

import com.cinepass.app.model.Perfil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String verCatalogo(Model model) {
        // Lista simulada en memoria para la presentación de hoy
        List<Perfil> listaPerfiles = new ArrayList<>();

        Perfil p1 = new Perfil();
        p1.setId(1L);
        p1.setPlataforma("Netflix");
        p1.setTipoPlan("Estándar - 1 Perfil (4K)");
        p1.setPrecio(12.00);
        p1.setEstado("DISPONIBLE");
        listaPerfiles.add(p1);

        Perfil p2 = new Perfil();
        p2.setId(2L);
        p2.setPlataforma("Max (HBO)");
        p2.setTipoPlan("Plan Mes Completo");
        p2.setPrecio(10.00);
        p2.setEstado("DISPONIBLE");
        listaPerfiles.add(p2);

        Perfil p3 = new Perfil();
        p3.setId(3L);
        p3.setPlataforma("Disney+");
        p3.setTipoPlan("Familiar - 1 Dispositivo");
        p3.setPrecio(11.00);
        p3.setEstado("DISPONIBLE");
        listaPerfiles.add(p3);

        model.addAttribute("perfiles", listaPerfiles);
        return "index"; // Carga index.html
    }

    @GetMapping("/checkout")
    public String verCheckout() {
        return "checkout"; // Carga checkout.html al hacer clic en reservar
    }
}
