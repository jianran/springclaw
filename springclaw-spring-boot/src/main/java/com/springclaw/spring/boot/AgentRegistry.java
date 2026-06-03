package com.springclaw.spring.boot;

import com.springclaw.core.Agent;
import com.springclaw.core.AgentConfig;

import java.util.Collection;

/**
 * Registry that manages all agents in the application.
 *
 * <p>Agents are created from configuration properties and/or {@code @Agent}-annotated beans.
 */
public interface AgentRegistry {

    /** Get an agent by ID, or null if not found. */
    Agent getAgent(String id);

    /** Get an agent by ID, throwing if not found. */
    Agent getAgentOrThrow(String id);

    /** Get all registered agents. */
    Collection<Agent> getAllAgents();

    /** Dynamically register a new agent. */
    Agent registerAgent(AgentConfig config);

    /** Remove an agent by ID. */
    void unregisterAgent(String id);
}
