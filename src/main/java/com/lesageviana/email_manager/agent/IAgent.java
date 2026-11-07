package com.lesageviana.email_manager.agent;

import com.lesageviana.email_manager.model.EmailMessage;
import java.util.Optional;

public interface IAgent {
    /**
     * Processes the email message using its stored context and system prompts.
     * The implementation should use the updated context when calling LMService.
     * @param email The original email message to respond to.
     * @return A ProcessResult containing the generated response or an empty result if failed.
     */
    String processEmail(EmailMessage email);

    record ProcessResult(boolean success, String proposedAnswer, String agentUsed) {}
}