package com.lesageviana.email_manager.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class LMService {
    private final ChatClient chatClient;

    public LMService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String chat(String prompt) {
        return chatClient.prompt()
          .user(userMessage -> userMessage.text(prompt))
          .call()
          .content();
    }
}
