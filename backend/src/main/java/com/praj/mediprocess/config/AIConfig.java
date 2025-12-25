package com.praj.mediprocess.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public ChatClient groqChatClient(ChatClient.Builder builder) {
        // Groq is OpenAI-compatible. We just override the base URL.
        return builder
                .defaultAdvisors()
                .build();
    }
}
