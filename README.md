# MediProc-AI: Agentic Clinical Decision Support System

MediProc-AI is a high-performance, multi-agent medical intelligence system designed to unify siloed clinical data. It transforms raw medical documents (images/PDFs) into actionable clinical insights using a **Triple-Database Hybrid RAG** architecture.

## 🚀 The Problem
Medical data is often trapped in inconsistent formats. Traditional systems struggle to combine **semantic meaning** (what a patient says), **structured facts** (lab results), and **clinical context** (historical relationships).

## 🧠 System Architecture
MediProc-AI solves this using three specialized data stores:
1. **Weaviate (Vector):** Semantic search engine for natural language discovery.
2. **NebulaGraph (Graph):** Knowledge graph for mapping patient-finding-history relationships.
3. **PostgreSQL (Relational):** The clinical source of truth for high-fidelity structured data.



## 🛠️ Tech Stack
* **Language:** Java 23 / Spring Boot 3.5.9
* **AI Orchestration:** Spring AI / LangGraph4j (7-Agent Workflow)
* **Databases:** Weaviate, NebulaGraph, PostgreSQL
* **Ingestion:** OCR (Tesseract/OpenCV)
* **LLM:** OpenAI GPT-4 / local ONNX Embeddings

## 🤖 Multi-Agent Workflow
The system utilizes 7 specialized AI agents to process records:
* **Supervisor:** Quality control and routing.
* **Extraction:** Identifying clinical entities.
* **Context:** Analyzing negations and timeframes.
* **Validation:** Cross-referencing findings.
* **Medical Coding:** ICD-10/SNOMED-CT mapping.
* **Explainability:** Evidence citation.
* **Synthesis:** Generating clinical executive summaries.

## 📡 API Capabilities
* **Full Analysis:** `POST /api/v1/process/full-analysis/{patientId}`
* **Semantic Search:** `GET /api/v1/query/search/semantic?query={text}`
* **Graph Network:** `GET /api/v1/query/patient-graph/{patientId}`
* **Clinical SOAP Recommendation:** `GET /api/v1/query/final-recommendation?query={text}`
* **Architecture Demo:** `GET /api/v1/query/system-architecture-demo`

## 📈 Key Outcomes
* **Automated SOAP Notes:** Generates standardized Subjective, Objective, Assessment, and Plan documentation.
* **Cross-Database Discovery:** Identifies comorbidities in the Knowledge Graph that semantic search might overlook.
* **Triage Prioritization:** Automatically flags CRITICAL cases based on extracted vitals and history.
