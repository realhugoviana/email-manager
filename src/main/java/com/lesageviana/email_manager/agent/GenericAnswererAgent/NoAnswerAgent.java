package com.lesageviana.email_manager.agent.GenericAnswererAgent;

import com.lesageviana.email_manager.agent.AnswererAgent;
import com.lesageviana.email_manager.service.LMService;

public class NoAnswerAgent extends AnswererAgent {
    public NoAnswerAgent(LMService lmService, String role, String systemPrompt, float[] embeddingDescription) {
        role = "ANSWERER";
        systemPrompt = "If it is either spam, phishing, felony or information mail that do NOT need any answer then: DO NOT ANSWER AT ALL. Leave the answer blank, keep it as it is.";

        super(lmService, role, systemPrompt, embeddingDescription);
    }
}
