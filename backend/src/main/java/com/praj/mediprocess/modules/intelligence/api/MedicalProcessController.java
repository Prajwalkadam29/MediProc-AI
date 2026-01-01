package com.praj.mediprocess.modules.intelligence.api;

import com.praj.mediprocess.modules.ingestion.service.OcrService;
import com.praj.mediprocess.modules.intelligence.schema.MedicalRecordState;
import com.praj.mediprocess.modules.intelligence.workflow.MedicalWorkflow;
import com.praj.mediprocess.modules.records.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/v1/process")
@RequiredArgsConstructor
public class MedicalProcessController {

    private final OcrService ocrService;
    private final MedicalWorkflow medicalWorkflow;
    private final MedicalRecordService storageService;

    /**
     * END-TO-END PIPELINE:
     * 1. OCR (Image to Text)
     * 2. LangGraph (Multi-Agent Analysis)
     * 3. Hybrid Storage (Postgres + Weaviate + NebulaGraph)
     */
    @PostMapping("/full-analysis/{patientId}")
    public ResponseEntity<String> analyzeAndStore(
            @PathVariable String patientId,
            @RequestParam("file") MultipartFile file) {

        try {
            // Step 1: Ingestion (Module 1)
            File tempFile = Files.createTempFile("process_", file.getOriginalFilename()).toFile();
            file.transferTo(tempFile);
            String rawText = ocrService.extractText(tempFile);

            // Step 2: Agentic Intelligence (Module 2)
            MedicalRecordState finalState = medicalWorkflow.run(rawText);

            // Step 3: Hybrid Persistence (Module 3)
            storageService.processAndStore(patientId, rawText, finalState);

            tempFile.delete();
            return ResponseEntity.ok("Record processed and synced across all databases. Entities found: "
                    + finalState.getExtractedEntities().size());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Pipeline failed: " + e.getMessage());
        }
    }
}
