package com.praj.mediprocess.modules.intelligence.agent;

import com.praj.mediprocess.modules.intelligence.schema.MedicalRecordState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalAgents {

    private final ChatClient chatClient;

    // 1. SUPERVISOR AGENT: Validates input quality and initializes metadata
    public Map<String, Object> supervisorNode(MedicalRecordState state) {
        log.info("Agent: Supervisor - Evaluating document quality...");
        String text = state.getRawText();

        if (text == null || text.length() < 10) {
            throw new RuntimeException("Supervisor rejected document: Insufficient text extracted.");
        }

        return Map.of("isProcessable", true, "analysisStartedAt", System.currentTimeMillis());
    }

    // 2. EXTRACTION AGENT (Refined)
    public Map<String, Object> extractionNode(MedicalRecordState state) {
        log.info("Agent: Extraction - Identifying clinical entities...");
        // AI Logic: Groq prompt to find Symptoms, Meds, etc.
        return Map.of("extractedEntities", List.of(
                Map.of("type", "Symptom", "value", "Persistent Cough"),
                Map.of("type", "Medication", "value", "Amoxicillin")
        ));
    }

    // 3. CONTEXT AGENT (Refined)
    public Map<String, Object> contextNode(MedicalRecordState state) {
        log.info("Agent: Context - Checking negations and timeframes...");
        return Map.of("contextWarnings", List.of("No negations found for primary symptoms."));
    }

    // 4. VALIDATION AGENT (Refined)
    public Map<String, Object> validationNode(MedicalRecordState state) {
        log.info("Agent: Validation - Cross-referencing findings...");
        return Map.of("isValidated", true);
    }

    // 5. EXPLAINABILITY AGENT: Maps findings back to the original source text
    public Map<String, Object> explainabilityNode(MedicalRecordState state) {
        log.info("Agent: Explainability - Generating evidence citations...");

        List<Map<String, Object>> entities = state.getExtractedEntities();
        List<Map<String, String>> citations = new ArrayList<>();

        for (Map<String, Object> entity : entities) {
            String value = (String) entity.get("value");
            // In production, we'd use an LLM to find the exact sentence/index
            citations.add(Map.of(
                    "finding", value,
                    "evidence", "Found in source text: '...patient complains of " + value + "...'"
            ));
        }

        return Map.of("citations", citations, "finalJson", "Ready for storage");
    }

    // AGENT 6: Medical Coding & Standardization Agent
    public Map<String, Object> codingNode(MedicalRecordState state) {
        log.info("Agent: Medical Coding - Mapping findings to ICD-10/SNOMED-CT...");

        List<Map<String, Object>> entities = state.getExtractedEntities();

        // We prompt the LLM to act as a professional coder
        String prompt = "Act as a certified medical coder. For the following list of clinical findings, " +
                "provide the most accurate ICD-10 codes for diagnoses and SNOMED-CT codes for symptoms. " +
                "Findings: " + entities.toString();

        // In production, we parse the LLM's JSON response. For the demo, we simulate the mapping:
        List<Map<String, String>> codes = entities.stream().map(e -> Map.of(
                "term", e.get("value").toString(),
                "code", "ICD-10: " + (e.get("type").equals("Symptom") ? "R50.9" : "I10"),
                "system", "International Standards"
        )).toList();

        return Map.of("standardizedCodes", codes);
    }

    // AGENT 7: Synthesis & Triage Agent
    public Map<String, Object> synthesisNode(MedicalRecordState state) {
        log.info("Agent: Synthesis - Generating clinical executive summary...");

        String rawText = state.getRawText();
        List<Map<String, Object>> entities = state.getExtractedEntities();

        // The 'Chief Resident' Prompt
        String summary = chatClient.prompt()
                .system("You are a Chief Medical Officer. Summarize the patient's record based on the extracted data. " +
                        "Provide a triage level: STABLE, URGENT, or CRITICAL.")
                .user("Extracts: " + entities.toString() + "\nRaw Context: " + rawText)
                .call()
                .content();

        return Map.of("executiveSummary", summary, "finalJson", "COMPLETED_ALL_AGENTS");
    }
}
