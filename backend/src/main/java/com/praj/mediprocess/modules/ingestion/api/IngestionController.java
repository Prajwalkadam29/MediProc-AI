package com.praj.mediprocess.modules.ingestion.api;

import com.praj.mediprocess.modules.ingestion.service.OcrService;
import com.praj.mediprocess.modules.ingestion.service.PdfService;
import com.praj.mediprocess.modules.ingestion.service.TextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;

@Slf4j
@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final OcrService ocrService;
    private final PdfService pdfService;
    private final TextService textService;

    @PostMapping("/upload")
    public ResponseEntity<String> processRecord(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return ResponseEntity.badRequest().body("Invalid file");

        try {
            String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
            log.info("Processing file: {} with extension: {}", fileName, extension);

            return switch (extension) {
                case ".md", ".yaml", ".yml" ->
                        ResponseEntity.ok(textService.extract(file));

                case ".pdf" -> {
                    File tempPdf = Files.createTempFile("med_query_", ".pdf").toFile();
                    file.transferTo(tempPdf);
                    String text = pdfService.extractTextFromPdf(tempPdf);
                    tempPdf.delete();
                    yield ResponseEntity.ok(text);
                }

                case ".png", ".jpg", ".jpeg" -> {
                    File tempImg = Files.createTempFile("med_img_", extension).toFile();
                    file.transferTo(tempImg);
                    String text = ocrService.extractText(tempImg);
                    tempImg.delete();
                    yield ResponseEntity.ok(text);
                }

                default -> ResponseEntity.badRequest().body("Unsupported file format: " + extension);
            };

        } catch (Exception e) {
            log.error("Ingestion error: ", e);
            return ResponseEntity.internalServerError().body("Processing failed: " + e.getMessage());
        }
    }
}
