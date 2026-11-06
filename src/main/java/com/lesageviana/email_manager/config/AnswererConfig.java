package com.lesageviana.email_manager.config;

import java.util.List;

public class AnswererConfig {
    private String agentName;
    private String className; // Fully qualified class name for reflection
    private String systemPrompt;
    private String initialContext;

    // Standard getters and setters for Jackson/Spring mapping
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getInitialContext() { return initialContext; }
    public void setInitialContext(String initialContext) { this.initialContext = initialContext; }

    public AnswererConfig() {}

    // Constructor for convenience, although usually Jackson handles mapping
    public AnswererConfig(String agentName, String className, String systemPrompt, String initialContext) {
        this.agentName = agentName;
        this.className = className;
        this.systemPrompt = systemPrompt;
        this.initialContext = initialContext;
    }
}