package com.cinepass.app.service;

import com.cinepass.app.model.entity.Perfil;
import com.cinepass.app.model.entity.Usuario;
import com.cinepass.app.repository.CuentaMaestraRepository;
import com.cinepass.app.repository.PerfilRepository;
import com.cinepass.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PerfilService {
    private final PerfilRepository perfiles;
    private final CuentaMaestraRepository cuentas;
    private final UsuarioRepository usuarios;

    public PerfilService(PerfilRepository perfiles, CuentaMaestraRepository cuentas, UsuarioRepository usuarios) {
        this.perfiles = perfiles;
        this.cuentas = cuentas;
        this.usuarios = usuarios;
    }

    @Transactional
    public void crear(long adminId, long cuentaId, String nombre, String pin, BigDecimal precio) {
        Usuario admin = usuarios.findById(adminId).orElseThrow(() -> new SecurityException("Acceso denegado"));
        if (admin.getEstado() != Usuario.EstadoUsuario.ACTIVO || !"ADMINISTRADOR".equals(admin.getRol().getNombre()))
            throw new SecurityException("Acceso denegado");
        if (nombre == null || nombre.isBlank() || nombre.length() > 80 || (pin != null && pin.length() > 100)
                || precio == null || precio.signum() < 0 || precio.scale() > 2 || precio.precision() > 10)
            throw new IllegalArgumentException("Datos del perfil inválidos");
        var cuenta = cuentas.findById(cuentaId).orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        if (cuenta.getEstado() != com.cinepass.app.model.entity.CuentaMaestra.EstadoCuenta.ACTIVA)
            throw new IllegalArgumentException("La cuenta no está activa");
        Perfil perfil = new Perfil();
        perfil.setCuentaMaestra(cuenta);
        perfil.setNombrePerfil(nombre.trim());
        perfil.setPinAcceso(pin);
        perfil.setPrecioVenta(precio);
        perfiles.saveAndFlush(perfil);
    }
}
