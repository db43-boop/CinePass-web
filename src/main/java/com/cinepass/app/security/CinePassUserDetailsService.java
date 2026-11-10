package com.cinepass.app.security;

import com.cinepass.app.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CinePassUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public CinePassUserDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        if (correo == null || correo.isBlank()) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        return usuarios.findByCorreoConRol(correo.trim().toLowerCase(Locale.ROOT))
                .filter(usuario -> usuario.getRol() != null
                        && ("ADMINISTRADOR".equals(usuario.getRol().getNombre())
                        || "CLIENTE".equals(usuario.getRol().getNombre())))
                .map(CinePassUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
