package com.lesageviana.email_manager.agent;

import com.lesageviana.email_manager.config.AnswererConfig;
import com.lesageviana.email_manager.service.LMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory responsible for dynamically loading and initializing AnswererAgents
 * using configuration stored in a JSON file via Java Reflection.
 */
@Service
public class AgentConfigLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LMService lmService;
    private List<AnswererAgent> loadedAgents;

    @Autowired
    public AgentConfigLoader(LMService lmService) {
        this.lmService = lmService;
        // Initialize agents upon bean creation
        this.loadedAgents = loadAgentsFromConfig();
    }

    /**
     * Reads the JSON configuration file and dynamically instantiates AnswererAgent instances.
     */
    private List<AnswererAgent> loadAgentsFromConfig() {
        List<AnswererConfig> configs = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("answerer_agents.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Could not find answerer_agents.json resource.");
            }
            // Read the list of configurations from the file stream
configs = objectMapper.readValue(inputStream, objectMapper.getTypeFactory().constructCollectionType(List.class, AnswererConfig.class));

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load or parse answerer_agents.json", e);
        }

        List<AnswererAgent> agents = new ArrayList<>();
        for (AnswererConfig config : configs) {
            try {
                // 1. Use reflection to get the target class
                Class<?> agentClass = Class.forName(config.getClassName());

                // 2. Find the constructor matching: (LMService, String, String)
                // The parameters must match the signature expected by AnswererAgent's primary constructor.
                Constructor<?> constructor = agentClass.getConstructor(LMService.class, String.class, String.class);

                // 3. Instantiate the agent using reflection
                Object instance = constructor.newInstance(this.lmService, config.getSystemPrompt(), config.getInitialContext());
                
                if (instance instanceof AnswererAgent) {
                    agents.add((AnswererAgent) instance);
                } else {
                    System.err.println("Warning: Class " + config.getClassName() + " does not inherit from AnswererAgent.");
                }

            } catch (ClassNotFoundException e) {
                System.err.println("Error loading agent class for [" + config.getAgentName() + "]: Class not found at path " + config.getClassName());
            } catch (Exception e) {
                System.err.println("Failed to initialize agent [" + config.getAgentName() + "] due to reflection error: " + e.getMessage());
            }
        }
        return agents;
    }

    public List<AnswererAgent> getLoadedAgents() {
        return loadedAgents;
    }
}