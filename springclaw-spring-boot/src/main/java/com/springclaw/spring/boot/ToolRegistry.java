package com.springclaw.spring.boot;

import com.springclaw.core.ToolDefinition;
import com.springclaw.core.ToolPolicy;

import java.util.List;

/**
 * Registry that manages tools across all agents with policy enforcement.
 */
public interface ToolRegistry {

    /** Get effective tools for an agent after policy filtering. */
    List<ToolDefinition> getEffectiveTools(String agentId);

    /** Register a tool for a specific agent. */
    void registerTool(String agentId, ToolDefinition tool);

    /** Register a tool globally for all agents. */
    void registerTool(ToolDefinition tool);

    /** Set the tool policy. */
    void setPolicy(ToolPolicy policy);
}
