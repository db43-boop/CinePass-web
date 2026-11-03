package com.cinepass.app.repository;

import com.cinepass.app.model.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    @Query("select p from Perfil p join fetch p.cuentaMaestra c join fetch c.plataforma where p.estado = com.cinepass.app.model.entity.Perfil.EstadoPerfil.DISPONIBLE and c.estado = com.cinepass.app.model.entity.CuentaMaestra.EstadoCuenta.ACTIVA and c.plataforma.estado = com.cinepass.app.model.entity.Plataforma.EstadoPlataforma.ACTIVA")
    List<Perfil> disponibles();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Perfil p where p.id = :id")
    Optional<Perfil> buscarParaActualizar(@Param("id") Long id);
}
