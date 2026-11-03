package com.cinepass.app.service;

import com.cinepass.app.model.Perfil;
import com.cinepass.app.repository.PerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class CatalogoService {
    private final PerfilRepository perfiles;

    public CatalogoService(PerfilRepository perfiles) {
        this.perfiles = perfiles;
    }

    @Transactional(readOnly = true)
    public List<Perfil> disponibles() {
        return perfiles.disponibles().stream().map(entidad -> {
            var cuenta = entidad.getCuentaMaestra();
            var plataforma = cuenta.getPlataforma();
            String nombre = plataforma.getNombre();
            String minusculas = nombre.toLowerCase(Locale.ROOT);
            Perfil vista = new Perfil();
            vista.setId(entidad.getId());
            vista.setPlataforma(nombre);
            vista.setSlug(minusculas.contains("netflix") ? "netflix" : minusculas.contains("disney") ? "disney" : "max");
            vista.setTipoPlan(cuenta.getTipoPlan());
            vista.setPrecio(entidad.getPrecioVenta().doubleValue());
            vista.setEstado(entidad.getEstado().name());
            vista.setImagenUrl(plataforma.getLogoUrl());
            return vista;
        }).toList();
    }
}
