package com.springclaw.spring.boot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for a single agent.
 */
public class AgentProperties {

    /** Display name for the agent. */
    private String name;

    /** Description of the agent's purpose. */
    private String description;

    /** System prompt that guides the agent's behavior. */
    private String systemPrompt;

    /** Model configuration for this agent. */
    private ModelProperties model = new ModelProperties();

    /** Tool configuration (allow/deny lists). */
    private ToolProperties tools = new ToolProperties();

    /** Memory configuration for this agent. */
    private MemoryProperties memory = new MemoryProperties();

    /** Additional metadata for the agent. */
    private Map<String, Object> metadata = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public ModelProperties getModel() {
        return model;
    }

    public void setModel(ModelProperties model) {
        this.model = model;
    }

    public ToolProperties getTools() {
        return tools;
    }

    public void setTools(ToolProperties tools) {
        this.tools = tools;
    }

    public MemoryProperties getMemory() {
        return memory;
    }

    public void setMemory(MemoryProperties memory) {
        this.memory = memory;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
