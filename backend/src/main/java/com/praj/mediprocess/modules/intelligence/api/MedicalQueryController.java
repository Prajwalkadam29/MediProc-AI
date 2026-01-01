package com.praj.mediprocess.modules.intelligence.api;

import com.praj.mediprocess.modules.intelligence.dto.ArchitectureAuditResponse;
import com.praj.mediprocess.modules.intelligence.service.ClinicalInsightService;
import com.praj.mediprocess.modules.records.service.GraphSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.praj.mediprocess.modules.records.repository.MedicalRecordRepository; // Your JPA repo
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/query")
@RequiredArgsConstructor
public class MedicalQueryController {

    private final VectorStore vectorStore;
    private final GraphSearchService graphSearchService;
    private final ClinicalInsightService insightService; // New Service
    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * 1. SEMANTIC SEARCH (Weaviate)
     * Finds medical records based on the MEANING of the query.
     */
    @GetMapping("/search/semantic")
    public List<String> semanticSearch(@RequestParam String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(3)
                .similarityThreshold(0.7)
                .build();

        // Similarity search returns List<org.springframework.ai.document.Document>
        return vectorStore.similaritySearch(request).stream()
                .map(doc -> {
                    // FIX: Use getText() to retrieve the document content
                    String text = doc.getText();

                    // Safely extract metadata to avoid "incompatible types"
                    String patientId = String.valueOf(doc.getMetadata().getOrDefault("patientId", "UNKNOWN"));
                    String triage = String.valueOf(doc.getMetadata().getOrDefault("triageLevel", "STABLE"));
                    String docType = String.valueOf(doc.getMetadata().getOrDefault("documentType", "REPORT"));

                    int snippetLength = Math.min(text.length(), 80);
                    return String.format("[%s] Patient: %s | Triage: %s | Match: %s...",
                            docType, patientId, triage, text.substring(0, snippetLength));
                })
                .collect(Collectors.toList());
    }

    /**
     * 2. GRAPH QUERY (NebulaGraph)
     * Finds all symptoms/entities connected to a specific patient.
     */
    @GetMapping("/patient-graph/{patientId}")
    public List<String> getPatientGraph(@PathVariable String patientId) {
        return graphSearchService.getConnectedSymptoms(patientId);
    }

    @GetMapping("/hybrid-insight")
    public Map<String, Object> getHybridInsight(@RequestParam String query) {
        Map<String, Object> response = new LinkedHashMap<>();

        // 1. Semantic Search
        var searchResults = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(1).similarityThreshold(0.6).build());

        if (searchResults.isEmpty()) {
            response.put("status", "NOT_FOUND");
            return response;
        }

        var topDoc = searchResults.get(0);

        // FIX: Clean the ID immediately
        String rawId = String.valueOf(topDoc.getMetadata().get("patientId"));
        log.info("DEBUG: Found Patient ID from Weaviate: [{}]", rawId);
        String cleanId = rawId.trim(); // Remove any accidental spaces from OCR or manual entry

        // 2. Knowledge Graph Discovery
        var graphNetwork = graphSearchService.getPatientNetwork(cleanId);

        // 3. Response Construction
        response.put("status", "SUCCESS");
        response.put("discovered_patient_id", cleanId);
        response.put("knowledge_graph_insights", graphNetwork); // Should now be populated

        return response;
    }

    @GetMapping("/final-recommendation")
    public Map<String, Object> getFinalReport(@RequestParam String query) {
        // Use Weaviate to find the patient from natural language
        var searchResults = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(1).build());

        if (searchResults.isEmpty()) return Map.of("error", "No patient matches this query.");

        String patientId = String.valueOf(searchResults.get(0).getMetadata().get("patientId"));

        // Use the new service to synthesize Postgres + Graph + AI
        String aiAdvice = insightService.generateRecommendation(patientId);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("patient", patientId);
        report.put("ai_clinical_recommendation", aiAdvice);
        return report;
    }

    @GetMapping("/system-architecture-demo")
    public ArchitectureAuditResponse getArchitectureDemo(@RequestParam String query) {
        // 1. WEAVIATE CONTRIBUTION (Semantic Retrieval)
        var searchResults = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(1).build());

        if (searchResults.isEmpty()) throw new RuntimeException("No data for demo");
        var topDoc = searchResults.get(0);
        String patientId = String.valueOf(topDoc.getMetadata().get("patientId"));

        // 2. NEBULAGRAPH CONTRIBUTION (Relationship Discovery)
        List<Map<String, String>> network = graphSearchService.getPatientNetwork(patientId);

        // 3. POSTGRES CONTRIBUTION (Relational Truth)
        var record = medicalRecordRepository.findTopByPatientIdOrderByProcessedAtDesc(patientId)
                .orElseThrow();

        // 4. LLM SYNTHESIS
        String aiAdvice = insightService.generateRecommendation(patientId);

        // BUILD THE AUDIT REPORT
        return ArchitectureAuditResponse.builder()
                .patientId(patientId)
                .query(query)
                .weaviate(ArchitectureAuditResponse.DatabaseContribution.builder()
                        .role("Semantic Search Engine: Located Patient via Natural Language Meaning")
                        .dataRetrieved("Top Match Content: " + topDoc.getText().substring(0, 100) + "...")
                        .build())
                .nebulaGraph(ArchitectureAuditResponse.DatabaseContribution.builder()
                        .role("Knowledge Graph: Discovered hidden clinical relationships and history")
                        .dataRetrieved(network)
                        .build())
                .postgres(ArchitectureAuditResponse.DatabaseContribution.builder()
                        .role("Relational System of Record: Fetched full structured clinical ground truth")
                        .dataRetrieved(record.getStructuredData())
                        .build())
                .finalAiSynthesis(aiAdvice)
                .build();
    }
}