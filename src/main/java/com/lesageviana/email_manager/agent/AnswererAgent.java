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
    private float[] embeddingDescription;
    /** The running context or memory of the agent (history of emails and responses) */
    protected StringBuilder context = new StringBuilder();

    // Constructor to inject necessary services and the agent's specific system prompt
    public AnswererAgent(LMService lmService, String role, String systemPrompt, float[] embeddingDescription) {
        this.lmService = lmService;
        this.role = role;
        this.systemPrompt = systemPrompt;
        this.embeddingDescription = makeEmbedDescription();
    }

    @Override
    public String getRole() {
        return this.role;
    }

    public float[] makeEmbedDescription() {
        // Combine the system prompt and current context for a comprehensive summary input
        String combinedContext = String.format("SYSTEM PROMPT: %s\n\nCURRENT CONTEXT/MEMORY:\n%s",
                this.systemPrompt, context.toString());

        // Get embeddings from the resulting summary text
        return lmService.getEmbeddings(combinedContext);
    }

    /**
     * Concrete agents must override canHandle to implement specific filtering logic.
     */
    public void updateContext(EmailMessage email) {
        // Append the new incoming email to the agent's context history
        context.append("\n--- NEW EMAIL RECEIVED ---\n");
        context.append("Sender: ").append(email.getSender()).append("\n");
        context.append("Subject: ").append(email.getSubject()).append("\n");
        context.append("Body:\n").append(email.getBody());

        this.embeddingDescription = makeEmbedDescription();
    }

    public float[] getEmbedDescription() {
        return this.embeddingDescription;
    }

    @Override
    public String processEmail(EmailMessage email) {
        // Incorporate the current stored context into the prompt for better LLM understanding
        updateContext(email);
        String combinedContext = String.format("AGENT SYSTEM PROMPT: %s\n\nCURRENT CONTEXT/MEMORY:\n%s",
                this.systemPrompt, context.toString());

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