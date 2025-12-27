package com.praj.mediprocess.modules.ingestion.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final OcrService ocrService;

    public String extractTextFromPdf(File pdfFile) throws Exception {
        StringBuilder fullText = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                // Render page to high-quality image (300 DPI is standard for medical OCR)
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);

                // Create temp image file for this page
                File tempPageFile = File.createTempFile("pdf_page_" + page, ".png");
                ImageIO.write(bim, "png", tempPageFile);

                // Reuse our existing OcrService (Preprocessing + Tesseract)
                String pageText = ocrService.extractText(tempPageFile);
                fullText.append("--- Page ").append(page + 1).append(" ---\n");
                fullText.append(pageText).append("\n");

                tempPageFile.delete();
            }
        }
        return fullText.toString();
    }
}
