package com.praj.mediprocess.modules.intelligence.api;

import com.praj.mediprocess.modules.intelligence.service.QueryService;
import com.praj.mediprocess.modules.intelligence.service.SpeechToTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/v1/voice")
@RequiredArgsConstructor
public class VoiceQueryController {

    private final SpeechToTextService sttService;
    private final QueryService queryService;

    @PostMapping("/ask")
    public ResponseEntity<String> askViaVoice(@RequestParam("audio") MultipartFile audioFile) {
        try {
            // 1. Convert Voice to Text
            File tempFile = Files.createTempFile("query_", ".mp3").toFile();
            audioFile.transferTo(tempFile);
            String transcript = sttService.transcribe(new FileSystemResource(tempFile));

            // 2. Analyze Text and Query Knowledge Base
            String answer = queryService.answerQuery(transcript);

            tempFile.delete();
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Voice Query Error: " + e.getMessage());
        }
    }
}