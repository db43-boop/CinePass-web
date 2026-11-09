package com.cinepass.app.controller;

import com.cinepass.app.dto.UsuarioResponse;
import com.cinepass.app.service.AutenticacionService;
import com.cinepass.app.service.CatalogoService;
import com.cinepass.app.service.ComprobanteService;
import com.cinepass.app.service.PedidoService;
import com.cinepass.app.service.ReservaService;
import com.cinepass.app.service.ValidacionPagoService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomeControllerTest {

    @Test
    void adminLoginAlsoAuthenticatesUserForOrders() {
        AutenticacionService autenticacion = mock(AutenticacionService.class);
        HomeController controller = new HomeController(
                mock(CatalogoService.class),
                autenticacion,
                mock(PedidoService.class),
                mock(ReservaService.class),
                mock(ComprobanteService.class),
                mock(ValidacionPagoService.class));
        MockHttpSession session = new MockHttpSession();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(autenticacion.autenticar("admin@cinepass.com", "password"))
                .thenReturn(new UsuarioResponse(7L, "Admin", "CinePass", "admin@cinepass.com", "ADMINISTRADOR"));

        String result = controller.loginSubmit(
                "admin@cinepass.com", "password", session, request, new ExtendedModelMap());

        assertEquals("redirect:/admin", result);
        assertEquals(7L, session.getAttribute("adminLogged"));
        assertEquals(7L, session.getAttribute("usuarioId"));
        verify(request).changeSessionId();
    }
}
