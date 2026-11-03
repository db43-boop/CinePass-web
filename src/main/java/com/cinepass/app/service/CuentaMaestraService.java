package com.cinepass.app.service;

import com.cinepass.app.model.entity.CuentaMaestra;
import com.cinepass.app.repository.CuentaMaestraRepository;
import com.cinepass.app.repository.PlataformaRepository;
import com.cinepass.app.repository.UsuarioRepository;
import com.cinepass.app.dto.CuentaMaestraRequest;
import com.cinepass.app.model.entity.Usuario;
import com.cinepass.app.model.entity.Plataforma;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaMaestraService {
    private final CuentaMaestraRepository cuentas;
    private final PlataformaRepository plataformas;
    private final UsuarioRepository usuarios;

    public CuentaMaestraService(CuentaMaestraRepository cuentas, PlataformaRepository plataformas, UsuarioRepository usuarios) {
        this.cuentas = cuentas;
        this.plataformas = plataformas;
        this.usuarios = usuarios;
    }

    @Transactional
    public Long crear(long administradorId, CuentaMaestraRequest datos) {
        validarAdministrador(administradorId);
        if (datos == null || datos.plataformaId() == null || datos.correo() == null
                || datos.correo().isBlank() || datos.correo().length() > 150
                || datos.password() == null || datos.password().isBlank() || datos.password().length() > 255
                || (datos.tipoPlan() != null && datos.tipoPlan().length() > 80)
                || (datos.observaciones() != null && datos.observaciones().length() > 500)
                || (datos.costo() != null && datos.costo().signum() < 0)
                || (datos.fechaAdquisicion() != null && datos.fechaVencimiento() != null
                    && datos.fechaVencimiento().isBefore(datos.fechaAdquisicion())))
            throw new IllegalArgumentException("Datos de la cuenta inválidos");
        Plataforma plataforma = plataformas.findById(datos.plataformaId())
                .orElseThrow(() -> new IllegalArgumentException("Plataforma no encontrada"));
        if (plataforma.getEstado() != Plataforma.EstadoPlataforma.ACTIVA)
            throw new IllegalArgumentException("La plataforma no está activa");
        if (cuentas.findAll().stream().anyMatch(c -> datos.password().equals(c.getPasswordCuenta())))
            throw new IllegalArgumentException("La contraseña ya está asignada a otra cuenta");
        CuentaMaestra cuenta = new CuentaMaestra();
        cuenta.setPlataforma(plataforma);
        cuenta.setCorreoCuenta(datos.correo().trim());
        cuenta.setPasswordCuenta(datos.password());
        cuenta.setTipoPlan(datos.tipoPlan());
        cuenta.setFechaAdquisicion(datos.fechaAdquisicion());
        cuenta.setFechaVencimiento(datos.fechaVencimiento());
        cuenta.setCosto(datos.costo() == null ? java.math.BigDecimal.ZERO : datos.costo());
        cuenta.setObservaciones(datos.observaciones());
        try { return cuentas.saveAndFlush(cuenta).getId(); }
        catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("No se pudo guardar la cuenta; revise los datos y la contraseña", ex);
        }
    }

    private void validarAdministrador(long administradorId) {
        Usuario usuario = usuarios.findById(administradorId)
                .orElseThrow(() -> new SecurityException("Se requiere un administrador activo"));
        if (usuario.getEstado() != Usuario.EstadoUsuario.ACTIVO || !"ADMINISTRADOR".equals(usuario.getRol().getNombre()))
            throw new SecurityException("Se requiere un administrador activo");
    }

    @Transactional
    public void cambiarPassword(long administradorId, long cuentaId, String password) {
        validarAdministrador(administradorId);
        if (password == null || password.isBlank() || password.length() > 255)
            throw new IllegalArgumentException("La contraseña es obligatoria y admite hasta 255 caracteres");
        CuentaMaestra cuenta = cuentas.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        boolean repetida = cuentas.findAll().stream()
                .anyMatch(otra -> !otra.getId().equals(cuentaId) && password.equals(otra.getPasswordCuenta()));
        if (repetida) throw new IllegalArgumentException("La contraseña ya está asignada a otra cuenta");
        cuenta.setPasswordCuenta(password);
        try {
            cuentas.saveAndFlush(cuenta);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("No se pudo guardar la contraseña; podría estar repetida", ex);
        }
    }
}
