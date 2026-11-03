package com.cinepass.app.dto;

import org.springframework.web.multipart.MultipartFile;

/** Datos del pago enviado por el cliente. */
public record ComprobanteRequest(String metodoPago, String numeroOperacion,
                                 String titular, MultipartFile archivo) {}
