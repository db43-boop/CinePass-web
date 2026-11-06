package com.cinepass.app.config;

import com.cinepass.app.model.entity.Rol;
import com.cinepass.app.repository.RolRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RolInitializer implements ApplicationRunner {
    private final RolRepository roles;

    public RolInitializer(RolRepository roles) {
        this.roles = roles;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearSiNoExiste("ADMINISTRADOR");
        crearSiNoExiste("CLIENTE");
    }

    private void crearSiNoExiste(String nombre) {
        if (roles.findByNombre(nombre).isPresent()) return;

        Rol rol = new Rol();
        rol.setNombre(nombre);
        roles.save(rol);
    }
}
