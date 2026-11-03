package com.cinepass.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class ProteccionFormularios implements WebMvcConfigurer {
    private final SecureRandom random = new SecureRandom();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                HttpSession session = request.getSession(true);
                String token = (String) session.getAttribute("formToken");
                if (token == null) {
                    byte[] bytes = new byte[32];
                    random.nextBytes(bytes);
                    token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                    session.setAttribute("formToken", token);
                }
                request.setAttribute("formToken", token);
                if ("POST".equalsIgnoreCase(request.getMethod())) {
                    String recibido = request.getParameter("_formToken");
                    if (recibido == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), recibido.getBytes(StandardCharsets.UTF_8))) {
                        response.sendError(403, "Formulario inválido o sesión vencida");
                        return false;
                    }
                }
                return true;
            }
        }).excludePathPatterns("/css/**", "/js/**", "/error");
    }
}
