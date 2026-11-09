package com.cinepass.app.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComprobantePdfConverterTest {
    private final ComprobantePdfConverter converter = new ComprobantePdfConverter();

    @Test
    void convertsPngImageIntoSinglePagePdf() throws Exception {
        BufferedImage image = new BufferedImage(120, 180, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);

        byte[] pdf = converter.convertir(png.toByteArray(), "image/png");

        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"));
        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());
        }
    }

    @Test
    void leavesPdfFilesUnchanged() throws Exception {
        byte[] pdf = converter.convertir(new byte[]{'%', 'P', 'D', 'F', '-'}, "application/pdf");

        assertArrayEquals(new byte[]{'%', 'P', 'D', 'F', '-'}, pdf);
    }

    @Test
    void rejectsUnsupportedMimeTypes() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertir(new byte[0], "image/gif"));
    }
}
