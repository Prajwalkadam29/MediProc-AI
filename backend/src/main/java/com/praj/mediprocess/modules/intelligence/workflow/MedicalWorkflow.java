package com.praj.mediprocess.modules.intelligence.workflow;

import com.praj.mediprocess.modules.intelligence.agent.MedicalAgents;
import com.praj.mediprocess.modules.intelligence.schema.MedicalRecordState;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalWorkflow {

    private final MedicalAgents agents;
    private CompiledGraph<MedicalRecordState> graph;

    @PostConstruct
    public void init() {
        try {
            this.graph = new StateGraph<>(MedicalRecordState::new)
                    // 1. Supervisor Node
                    .addNode("supervisor", node_async(agents::supervisorNode))
                    // 2. Extraction Node
                    .addNode("extraction", node_async(agents::extractionNode))
                    // 3. Context Node
                    .addNode("context", node_async(agents::contextNode))
                    // 4. Validation Node
                    .addNode("validation", node_async(agents::validationNode))
                    // 5. NEW: Medical Coding Node
                    .addNode("coding", node_async(agents::codingNode))
                    // 6. Explainability Node
                    .addNode("explainability", node_async(agents::explainabilityNode))
                    // 7. NEW: Synthesis & Triage Node
                    .addNode("synthesis", node_async(agents::synthesisNode))

                    // Define the 7-Agent Flow
                    .addEdge(START, "supervisor")
                    .addEdge("supervisor", "extraction")
                    .addEdge("extraction", "context")
                    .addEdge("context", "validation")
                    .addEdge("validation", "coding")       // Point to Coding
                    .addEdge("coding", "explainability")   // Coding to Explainability
                    .addEdge("explainability", "synthesis")// Explainability to Synthesis
                    .addEdge("synthesis", END)             // Synthesis is the final stop
                    .compile();

            log.info("7-Agent Medical Intelligence Workflow fully operational.");
        } catch (Exception e) {
            log.error("Workflow initialization failed", e);
            throw new RuntimeException(e);
        }
    }

    public MedicalRecordState run(String rawText) {
        // Initial state with raw OCR text
        Map<String, Object> initialData = Map.of(
                "rawText", rawText,
                "isValidated", false
        );

        try {
            log.info("Starting agentic analysis for medical record...");
            return graph.invoke(initialData)
                    .orElseThrow(() -> new RuntimeException("AI Workflow failed to reach the final state."));
        } catch (Exception e) {
            log.error("Workflow execution failed during runtime", e);
            throw new RuntimeException("AI Analysis failed: " + e.getMessage());
        }
    }
}
