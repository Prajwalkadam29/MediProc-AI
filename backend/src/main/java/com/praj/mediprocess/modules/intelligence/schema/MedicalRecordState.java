package com.praj.mediprocess.modules.intelligence.schema;

import org.bsc.langgraph4j.state.AgentState;
import java.util.List;
import java.util.Map;

public class MedicalRecordState extends AgentState {

    public MedicalRecordState(Map<String, Object> initData) {
        super(initData);
    }

    public String getRawText() { return (String) data().get("rawText"); }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getExtractedEntities() {
        return (List<Map<String, Object>>) data().get("extractedEntities");
    }

    // New: Standardized Codes (ICD-10/SNOMED)
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getStandardizedCodes() {
        return (List<Map<String, String>>) data().get("standardizedCodes");
    }

    // New: Executive Summary
    public String getExecutiveSummary() {
        return (String) data().get("executiveSummary");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getCitations() {
        return (List<Map<String, String>>) data().get("citations");
    }

    public String getFinalJson() { return (String) data().get("finalJson"); }
}
