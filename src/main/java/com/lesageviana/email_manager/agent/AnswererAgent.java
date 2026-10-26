package com.lesageviana.email_manager.agent;

import com.lesageviana.email_manager.model.EmailMessage;
import com.lesageviana.email_manager.service.LMService;
import org.springframework.stereotype.Component;

/**
 * Abstract base class for all answerer agents, providing common structure
 * and handling the interaction with the LLM via LMService.
 */
@Component
public abstract class AnswererAgent implements IAgent {
    private final String role;
    protected final LMService lmService;
    private final String systemPrompt; // Agent's mandatory system prompt
    /** The running context or memory of the agent (history of emails and responses) */
    protected StringBuilder context = new StringBuilder();

    // Constructor to inject necessary services and the agent's specific system prompt
    public AnswererAgent(LMService lmService, String role, String systemPrompt) {
        this.lmService = lmService;
        this.role = role;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public String getRole() {
        return this.role;
    }

    /**
     * Concrete agents must override canHandle to implement specific filtering logic.
     */
    public void updateContext(EmailMessage email) {
        // Append the new incoming email to the agent's context history
        context.append("\n--- NEW EMAIL RECEIVED ---\n");
        context.append("Subject: ").append(email.getSubject()).append("\n");
        context.append("Body:\n").append(email.getBody());
    }

    public String getDescription() {
        // Description combines the core role/prompt with the accumulated context memory.
        String contextSummary = context.length() > 100 ? "Context updated (see details in prompt)." : "";
        return String.format("Agent Role: %s. Purpose: %s. Context Status: %s",
                getRole(), this.systemPrompt, contextSummary);
    }

    @Override
    public String processEmail(EmailMessage email) {
        // Incorporate the current stored context into the prompt for better LLM understanding
        String combinedContext = String.format("AGENT SYSTEM PROMPT: %s\n\nCURRENT CONTEXT/MEMORY:\n%s\n\n--- NEW INCOMING EMAIL ---\nSubject: %s\nBody: %s",
                this.systemPrompt, context.toString(), email.getSubject(), email.getBody());

        try {
            // Call the underlying LLM service with the comprehensive prompt including context
            String proposedAnswer = lmService.chat(combinedContext);
            return proposedAnswer;
        } catch (Exception e) {
            System.err.println("Error processing email in agent " + this.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }
}