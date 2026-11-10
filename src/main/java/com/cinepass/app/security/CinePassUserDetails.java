package com.cinepass.app.security;

import com.cinepass.app.model.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CinePassUserDetails implements UserDetails {
    private final Long id;
    private final String correo;
    private final String passwordHash;
    private final String rol;
    private final boolean activo;

    public CinePassUserDetails(Usuario usuario) {
        this.id = usuario.getId();
        this.correo = usuario.getCorreo();
        this.passwordHash = usuario.getPasswordHash();
        this.rol = usuario.getRol().getNombre();
        this.activo = usuario.getEstado() == Usuario.EstadoUsuario.ACTIVO;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
