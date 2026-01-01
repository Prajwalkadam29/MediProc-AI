package com.praj.mediprocess.modules.intelligence.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public String answerQuery(String voiceTranscript) {
        // Fix 1: Use SearchRequest.builder() instead of the .query() static method
        SearchRequest searchRequest = SearchRequest.builder()
                .query(voiceTranscript)
                .topK(3)
                .build();

        // 1. Semantic Search: Find relevant medical records in Weaviate
        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

        // Fix 2: Use Document::getText instead of Document::getContent
        String context = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 2. RAG (Retrieval-Augmented Generation): Answer using Groq
        return chatClient.prompt()
                .system("""
                    You are a senior clinical assistant. 
                    Use the provided medical context to answer the doctor's question. 
                    Always prioritize clinical accuracy and professional tone.
                    If the answer is not contained within the context, clearly state: 
                    'I don't have enough information in the patient's records to answer this.'
                    """)
                .user(u -> u.text("Context from Records: \n {context} \n\n Question: {question}")
                        .param("context", context)
                        .param("question", voiceTranscript))
                .call()
                .content();
    }
}
