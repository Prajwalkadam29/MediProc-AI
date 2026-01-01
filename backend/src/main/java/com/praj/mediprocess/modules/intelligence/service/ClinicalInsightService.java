package com.praj.mediprocess.modules.intelligence.service;

import com.praj.mediprocess.modules.records.repository.MedicalRecordRepository;
import com.praj.mediprocess.modules.records.service.GraphSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClinicalInsightService {

    private final MedicalRecordRepository postgresRepo;
    private final GraphSearchService graphService;
    private final ChatClient chatClient; // Spring AI Chat Client

    public String generateRecommendation(String patientId) {
        // 1. Fetch Relational Data from Postgres (The detailed Source of Truth)
        var record = postgresRepo.findTopByPatientIdOrderByProcessedAtDesc(patientId)
                .orElseThrow(() -> new RuntimeException("Record not found in Postgres"));

        // 2. Fetch Relationship Data from NebulaGraph (Comorbidities & Connections)
        List<Map<String, String>> network = graphService.getPatientNetwork(patientId);

        // 3. Build the Standardized SOAP Prompt
        String prompt = String.format(
                "SYSTEM: You are a Senior Clinical Documentation Expert. Generate a professional SOAP note.\n" +
                        "CONTEXT FROM KNOWLEDGE GRAPH: %s\n" +
                        "STRUCTURED DATA FROM POSTGRES: %s\n\n" +
                        "TASK: Create a clinical summary for Patient %s in the following format:\n" +
                        "1. SUBJECTIVE: Summarize patient symptoms and history from the data.\n" +
                        "2. OBJECTIVE: List specific clinical findings, lab results (like HbA1c), and vitals.\n" +
                        "3. ASSESSMENT: Provide a diagnostic reasoning based on the symptoms and graph-linked comorbidities.\n" +
                        "4. PLAN: Detail the next clinical steps, medication adjustments, and follow-up urgency.\n\n" +
                        "STYLE: Use professional clinical language (e.g., 'patient presents with', 'clinical evidence suggests').",
                network, record.getStructuredData(), patientId
        );

        return chatClient.prompt().user(prompt).call().content();
    }
}
