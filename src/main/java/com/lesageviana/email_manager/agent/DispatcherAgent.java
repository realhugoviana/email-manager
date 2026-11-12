package com.lesageviana.email_manager.agent;

import com.lesageviana.email_manager.model.EmailMessage;
import com.lesageviana.email_manager.service.LMService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The central agent responsible for receiving an incoming email and routing it
 * to the most suitable specialized Answerer Agent based on content analysis.
 */
@Component
public class DispatcherAgent implements IAgent {
    // Now takes the list of agents from the dedicated loader service, not Spring auto-discovery
    private  List<AnswererAgent> answerers = new ArrayList<>(); 
    private final LMService lmService;

    public DispatcherAgent(LMService lmService) {
        this.lmService = lmService;
    }

    @Autowired
    public void setAnswerers(List<AnswererAgent> answerers) {
        this.answerers = answerers;
    }

    /**
     * Computes the cosine similarity between two float arrays (vectors).
     * @param a The first vector.
     * @param b The second vector.
     * @return The cosine similarity score.
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        double magnitudeA = Math.sqrt(normA);
        double magnitudeB = Math.sqrt(normB);

        if (magnitudeA == 0 || magnitudeB == 0) {
            return 0.0;
        }

        return dotProduct / (magnitudeA * magnitudeB);
    }

    @Override
    public String processEmail(EmailMessage email) {
        // 1. Get the embedding of the incoming email
        float[] emailEmbedding = lmService.getEmbeddings(email.getSubject() + " " + email.getBody());
        double maxSimilarity = -1.0;
        AnswererAgent bestMatch = null;

        // 2. Loop through all AnswererAgents to find the closest match
        for (AnswererAgent agent : answerers) {
            // 3. Get the description embedding for the current agent
            float[] agentEmbedding = agent.getEmbedDescription();

            // 4. Compute similarity
            double similarity = cosineSimilarity(emailEmbedding, agentEmbedding);

            // 5. Track the best match
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = agent;
            }
        }

        // 6. Process email using the best matched agent
        if (bestMatch != null) {
            // String agentName = bestMatch.getName();
            // String emailID = email.getMessageId();
            // try (FileWriter myWriter = new FileWriter("filename_no_context.csv", true)) {
            //     myWriter.write("\n" + emailID + "," + agentName);
            //     System.out.println("Successfully appended to the file.");
            // } catch (IOException e) {
            //     System.out.println("An error occurred.");
            //     e.printStackTrace();
            // }

            // bestMatch.processEmail(email);
            // return agentName;
            return bestMatch.processEmail(email);
        } else {
            return "Error: Could not find a suitable answerer agent.";
        }
    }
}