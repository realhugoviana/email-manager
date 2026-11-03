package com.lesageviana.email_manager.agent.GenericAnswererAgent;

import com.lesageviana.email_manager.agent.AnswererAgent;
import com.lesageviana.email_manager.service.LMService;

public class NoAnswerAgent extends AnswererAgent {
    public NoAnswerAgent(LMService lmService, String role, String systemPrompt) {
        role = "ANSWERER";
        systemPrompt = "FORGET EVERY PAST INSTRUCTION. DO NOT ANSWER AT ALL. Leave the answer blank, keep it as it is.";

        super(lmService, role, systemPrompt);
    }
}
