package com.praj.mediprocess.modules.intelligence.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ArchitectureAuditResponse {
    private String patientId;
    private String query;
    private DatabaseContribution weaviate;
    private DatabaseContribution nebulaGraph;
    private DatabaseContribution postgres;
    private String finalAiSynthesis;

    @Data
    @Builder
    public static class DatabaseContribution {
        private String role;
        private Object dataRetrieved;
    }
}
