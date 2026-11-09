package com.cinepass.app.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Component
public class ComprobantePdfConverter {
    private static final long MAX_IMAGE_PIXELS = 30_000_000;
    private static final float PAGE_MARGIN = 24;

    public byte[] convertir(byte[] archivo, String tipoMime) throws IOException {
        if ("application/pdf".equals(tipoMime)) return archivo;
        if (!"image/png".equals(tipoMime) && !"image/jpeg".equals(tipoMime))
            throw new IllegalArgumentException("Formato de comprobante no compatible");

        BufferedImage image = leerImagen(archivo);
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            boolean horizontal = image.getWidth() > image.getHeight();
            float pageWidth = horizontal ? PDRectangle.A4.getHeight() : PDRectangle.A4.getWidth();
            float pageHeight = horizontal ? PDRectangle.A4.getWidth() : PDRectangle.A4.getHeight();
            PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
            document.addPage(page);

            var pdfImage = "image/jpeg".equals(tipoMime)
                    ? JPEGFactory.createFromStream(document, new ByteArrayInputStream(archivo))
                    : LosslessFactory.createFromImage(document, image);
            float scale = Math.min(
                    (pageWidth - PAGE_MARGIN * 2) / image.getWidth(),
                    (pageHeight - PAGE_MARGIN * 2) / image.getHeight());
            float imageWidth = image.getWidth() * scale;
            float imageHeight = image.getHeight() * scale;
            float x = (pageWidth - imageWidth) / 2;
            float y = (pageHeight - imageHeight) / 2;

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.drawImage(pdfImage, x, y, imageWidth, imageHeight);
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private BufferedImage leerImagen(byte[] archivo) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(archivo))) {
            if (input == null) throw new IllegalArgumentException("No se pudo leer la imagen del comprobante");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("La imagen del comprobante no es válida");

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_IMAGE_PIXELS)
                    throw new IllegalArgumentException("La imagen del comprobante tiene dimensiones no válidas");
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        }
    }
}
