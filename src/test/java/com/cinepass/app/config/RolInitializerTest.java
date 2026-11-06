package com.cinepass.app.config;

import com.cinepass.app.model.entity.Rol;
import com.cinepass.app.repository.RolRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RolInitializerTest {

    @Test
    void createsMissingRolesWithoutDependingOnTheirIds() {
        RolRepository roles = mock(RolRepository.class);
        when(roles.findByNombre(anyString())).thenReturn(Optional.empty());

        new RolInitializer(roles).run(null);

        verify(roles).save(org.mockito.ArgumentMatchers.argThat(rol -> "ADMINISTRADOR".equals(rol.getNombre())));
        verify(roles).save(org.mockito.ArgumentMatchers.argThat(rol -> "CLIENTE".equals(rol.getNombre())));
    }

    @Test
    void preservesRolesThatAlreadyExist() {
        RolRepository roles = mock(RolRepository.class);
        when(roles.findByNombre(anyString())).thenReturn(Optional.of(new Rol()));

        new RolInitializer(roles).run(null);

        verify(roles, never()).save(org.mockito.ArgumentMatchers.any(Rol.class));
    }
}
