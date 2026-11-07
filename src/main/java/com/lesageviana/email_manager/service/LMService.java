package com.lesageviana.email_manager.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;


@Service
public class LMService {
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    public LMService(ChatClient.Builder chatBuilder, EmbeddingModel embeddingModel) {
        this.chatClient = chatBuilder.build();
        this.embeddingModel = embeddingModel;
    }

    public String chat(String prompt) {
        return chatClient.prompt()
          .user(userMessage -> userMessage.text(prompt))
          .call()
          .content();
    }

    public float[] getEmbeddings(String text) {
        return embeddingModel.embed(text);
    }
}
