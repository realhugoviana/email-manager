package com.lesageviana.email_manager.agent;

import com.lesageviana.email_manager.model.EmailMessage;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * The central agent responsible for receiving an incoming email and routing it
 * to the most suitable specialized Answerer Agent based on content analysis.
 */
@Component
public class DispatcherAgent implements IAgent {
    private final List<AnswererAgent> answerers; // All available concrete Answerer Agents

    // Spring will automatically inject all beans implementing IAgent (the specific Answerers)
    public DispatcherAgent(List<AnswererAgent> answerers) {
        this.answerers = answerers;
    }

    @Override
    public String getRole() {
        return "DISPATCHER";
    }

    @Override
    public String processEmail(EmailMessage email) {
        // Logic for deciding which agent is "best" based on content matching against descriptions.
        AnswererAgent bestMatchAgent = null;
        // Heuristic: Score agents based on keyword overlap between the incoming email and its description/system prompt.
        for (AnswererAgent agent : answerers) {
            bestMatchAgent = agent;

        }
        try {

            return bestMatchAgent.processEmail(email);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}