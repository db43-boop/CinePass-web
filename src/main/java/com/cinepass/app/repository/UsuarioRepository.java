package com.cinepass.app.repository;

import com.cinepass.app.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    @Query("select u from Usuario u join fetch u.rol where u.correo = :correo")
    Optional<Usuario> findByCorreoConRol(@Param("correo") String correo);
}
