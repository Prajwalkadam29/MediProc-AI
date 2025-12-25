package com.praj.mediprocess.config;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStoreOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.weaviate.host}")
    private String host;

    @Value("${spring.ai.vectorstore.weaviate.object-class:MedicalKnowledge_Final}")
    private String objectClass;

    @Bean
    public WeaviateClient weaviateClient() {
        // This ensures both the schema init and the store use Port 8081
        Config config = new Config("http", host);
        return new WeaviateClient(config);
    }

    @Bean
    public WeaviateVectorStore vectorStore(WeaviateClient weaviateClient, EmbeddingModel embeddingModel) {
        // 1. Create the options object (This is where the class name lives)
        WeaviateVectorStoreOptions options = new WeaviateVectorStoreOptions();
        options.setObjectClass(objectClass);

        // 2. Use the builder that your compiler is asking for
        return WeaviateVectorStore.builder(weaviateClient, embeddingModel)
                .options(options)
                .build();
    }
}