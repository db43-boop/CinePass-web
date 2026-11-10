package com.cinepass.app.controller;

import com.cinepass.app.config.SecurityConfig;
import com.cinepass.app.model.entity.Rol;
import com.cinepass.app.model.entity.Usuario;
import com.cinepass.app.security.CinePassUserDetails;
import com.cinepass.app.service.CatalogoService;
import com.cinepass.app.service.ComprobanteService;
import com.cinepass.app.service.PedidoService;
import com.cinepass.app.service.ReservaService;
import com.cinepass.app.service.ValidacionPagoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HomeController.class)
@Import(SecurityConfig.class)
class SecurityAccessTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private CatalogoService catalogoService;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private ReservaService reservaService;

    @MockitoBean
    private ComprobanteService comprobanteService;

    @MockitoBean
    private ValidacionPagoService validacionPagoService;

    @Test
    void unauthenticatedUsersAreRedirectedToLoginForPrivateRoutes() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/cliente"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void clientCannotEnterAdministrativeRoutes() throws Exception {
        mockMvc.perform(get("/admin").with(user(userWithRole("CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCannotEnterClientRoutes() throws Exception {
        mockMvc.perform(get("/cliente").with(user(userWithRole("ADMINISTRADOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void eachRoleSeesOnlyItsOwnDashboardNavigation() throws Exception {
        when(pedidoService.listar(12L)).thenReturn(List.of());
        when(validacionPagoService.pendientes(42L)).thenReturn(List.of());

        mockMvc.perform(get("/cliente").with(user(userWithRole("CLIENTE"))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mis pedidos")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Panel administrativo"))));
        mockMvc.perform(get("/admin").with(user(userWithRole("ADMINISTRADOR"))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Panel administrativo")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Mis pedidos"))));
    }

    @Test
    void loginRedirectsUsersToTheirOwnDashboard() throws Exception {
        when(userDetailsService.loadUserByUsername("admin@cinepass.test"))
                .thenReturn(userWithRole("ADMINISTRADOR", passwordEncoder.encode("secret")));
        when(userDetailsService.loadUserByUsername("cliente@cinepass.test"))
                .thenReturn(userWithRole("CLIENTE", passwordEncoder.encode("secret")));

        mockMvc.perform(post("/login")
                        .param("correo", "admin@cinepass.test")
                        .param("password", "secret")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
        mockMvc.perform(post("/login")
                        .param("correo", "cliente@cinepass.test")
                        .param("password", "secret")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cliente"));
    }

    @Test
    void logoutRequiresCsrfProtection() throws Exception {
        mockMvc.perform(post("/salir").with(user(userWithRole("CLIENTE", "unused"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/salir")
                        .with(user(userWithRole("CLIENTE", "unused")))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    private CinePassUserDetails userWithRole(String role) {
        return userWithRole(role, "unused");
    }

    private CinePassUserDetails userWithRole(String role, String passwordHash) {
        Rol rol = new Rol();
        rol.setNombre(role);
        Usuario usuario = new Usuario();
        usuario.setId("CLIENTE".equals(role) ? 12L : 42L);
        usuario.setCorreo(role.toLowerCase() + "@cinepass.test");
        usuario.setPasswordHash(passwordHash);
        usuario.setRol(rol);
        return new CinePassUserDetails(usuario);
    }
}
