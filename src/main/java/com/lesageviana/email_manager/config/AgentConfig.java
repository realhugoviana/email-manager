package com.lesageviana.email_manager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesageviana.email_manager.agent.AnswererAgent;
import com.lesageviana.email_manager.service.LMService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Configuration
// Ensure this package is scanned to make the config class available
@ComponentScan(basePackages = "com.lesageviana.email_manager") 
public class AgentConfig {

    @Autowired
    private LMService lmService;

    /**
     * Reads answerer_agents.json and programmatically registers all AnswererAgent instances
     * as Spring Beans, making them available for injection into DispatcherAgent.
     */
        /**
     * Provides the compiled list of all Answerer Agents as a Spring Bean.
     */
    @Bean
    public List<AnswererAgent> answerers(LMService lmService) {
        System.out.println("--- AgentConfig: Building and registering agent list bean ---");
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Use the absolute path to ensure Jackson can find the resource file
            File jsonFile = new File("src/main/resources/answerer_agents.json"); 
            if (!jsonFile.exists()) {
                System.err.println("Error: answerer_agents.json not found at expected path.");
                return new ArrayList<>(); // Return empty list on failure
            }

            List<AgentConfigData> agentConfigs = mapper.readValue(jsonFile, 
                    mapper.getTypeFactory().constructCollectionType(List.class, AgentConfigData.class));

            List<AnswererAgent> answerers = new ArrayList<>();
            for (AgentConfigData config : agentConfigs) {
                // Instantiates the AnswererAgent using its required dependencies and loaded config data
                try {
                    AnswererAgent agent = new AnswererAgent(lmService, config.systemPrompt(), config.initialContext());
                    answerers.add(agent);
                    System.out.println("Successfully initialized agent: " + config.agentName());
                } catch (Exception e) {
                    System.err.println("Failed to initialize agent " + config.agentName() + ": " + e.getMessage());
                }
            }
            return answerers;

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during Agent List Bean creation: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on critical failure
        }
    }


    /**
     * Helper class to map JSON structure to Java objects.
     */
    private static class AgentConfigData {
        private String agentName;
        private String systemPrompt;
        private String initialContext;

        // Getters and Setters required for Jackson mapping
        public String agentName() { return agentName; }
        public void setAgentName(String agentName) { this.agentName = agentName; }
        public String systemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
        public String initialContext() { return initialContext; }
        public void setInitialContext(String initialContext) { this.initialContext = initialContext; }
    }
}