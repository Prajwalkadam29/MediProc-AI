package com.praj.mediprocess.config;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeaviateSchemaConfig {

    private final WeaviateClient weaviateClient; // Injected automatically from VectorStoreConfig

    @Value("${spring.ai.vectorstore.weaviate.object-class:MedicalKnowledge_Final}")
    private String className;

    @PostConstruct
    public void initSchema() {
        try {
            log.info("Initializing explicit schema for Weaviate class: {}...", className);

            Result<Boolean> exists = weaviateClient.schema().exists().withClassName(className).run();

            // ADD THIS CHECK: If there's an error object, it means the connection failed
            if (exists.getError() != null) {
                log.error("Could not connect to Weaviate. Ensure Docker is running on Port 8081. Error: {}",
                        exists.getError().getMessages());
                return;
            }

            if (exists.getResult() != null && exists.getResult()) {
                log.info("Schema {} already exists.", className);
                return;
            }

            log.info("Creating fresh explicit Weaviate schema for {}...", className);

            WeaviateClass medicalClass = WeaviateClass.builder()
                    .className(className)
                    .vectorizer("none")
                    .properties(List.of(
                            Property.builder().name("patientId").dataType(List.of(DataType.TEXT)).build(),
                            Property.builder().name("documentType").dataType(List.of(DataType.TEXT)).build(),
                            Property.builder().name("triageLevel").dataType(List.of(DataType.TEXT)).build()
                    ))
                    .build();

            Result<Boolean> result = weaviateClient.schema().classCreator().withClass(medicalClass).run();

            if (result.getError() != null) {
                log.error("Failed to create Weaviate class: {}", result.getError().getMessages());
            } else {
                log.info("SUCCESS: Created explicit schema {} for 384-dimension vectors.", className);
            }

        } catch (Exception e) {
            log.error("Fatal error connecting to Weaviate during schema init", e);
        }
    }
}