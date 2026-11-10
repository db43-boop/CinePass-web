package com.cinepass.app.service;

import com.cinepass.app.dto.RegistroUsuarioRequest;
import com.cinepass.app.dto.UsuarioResponse;
import com.cinepass.app.model.entity.Usuario;
import com.cinepass.app.repository.RolRepository;
import com.cinepass.app.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class AutenticacionService {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;

    public AutenticacionService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroUsuarioRequest datos) {
        if (datos == null || datos.nombres() == null || datos.nombres().isBlank()
                || datos.apellidos() == null || datos.apellidos().isBlank()
                || datos.correo() == null || datos.correo().isBlank()
                || datos.password() == null || datos.password().length() < 8
                || datos.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Datos inválidos; la contraseña debe tener al menos 8 caracteres");
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarios.findByCorreo(correo).isPresent()) throw new IllegalArgumentException("El correo ya está registrado");
        Usuario usuario = new Usuario();
        usuario.setNombres(datos.nombres().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setPasswordHash(encoder.encode(datos.password()));
        usuario.setTelefono(datos.telefono());
        usuario.setRol(roles.findByNombre("CLIENTE").orElseThrow(() -> new IllegalStateException("Falta el rol CLIENTE")));
        return response(usuarios.save(usuario));
    }

    private UsuarioResponse response(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombres(), usuario.getApellidos(),
                usuario.getCorreo(), usuario.getRol().getNombre());
    }
}
