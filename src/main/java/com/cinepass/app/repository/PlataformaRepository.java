package com.cinepass.app.repository;

import com.cinepass.app.model.entity.Plataforma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlataformaRepository extends JpaRepository<Plataforma, Long> {
    java.util.List<Plataforma> findByEstado(Plataforma.EstadoPlataforma estado);
}
