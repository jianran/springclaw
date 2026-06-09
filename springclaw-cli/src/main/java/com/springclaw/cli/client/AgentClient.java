package com.springclaw.cli.client;

import com.springclaw.core.TokenUsage;
import reactor.core.publisher.Flux;

import java.util.Collection;

/**
 * Unified agent client interface that abstracts local vs remote execution.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@link LocalAgentClient} — uses AgentRegistry directly (in-process)</li>
 *   <li>{@link RemoteAgentClient} — calls the gateway via HTTP/WebSocket</li>
 * </ul>
 */
public interface AgentClient {

    /**
     * Get info about a specific agent.
     *
     * @param agentId the agent identifier
     * @return agent info, or null if not found
     */
    AgentInfo getAgentInfo(String agentId);

    /**
     * List all available agents.
     *
     * @return collection of agent info
     */
    Collection<AgentInfo> listAgents();

    /**
     * Send a prompt and get a complete response.
     *
     * @param agentId the agent to use
     * @param message the user's message
     * @param sessionId the session ID (null to create new)
     * @return the prompt result
     */
    PromptResult prompt(String agentId, String message, String sessionId);

    /**
     * Send a prompt and stream the response token-by-token.
     *
     * @param agentId the agent to use
     * @param message the user's message
     * @param sessionId the session ID (null to create new)
     * @return a Flux of response text chunks
     */
    Flux<String> streamPrompt(String agentId, String message, String sessionId);

    /**
     * Reset the conversation history for a session.
     *
     * @param agentId the agent ID
     * @param sessionId the session ID to reset
     */
    void resetSession(String agentId, String sessionId);

    /**
     * Get tool definitions for an agent.
     *
     * @param agentId the agent ID
     * @return collection of tool definitions
     */
    Collection<ToolDefinition> listTools(String agentId);

    /**
     * Agent info record for display purposes.
     */
    record AgentInfo(
        String id,
        String name,
        String description,
        String systemPrompt
    ) {}

    /**
     * Tool definition record for display purposes.
     */
    record ToolDefinition(
        String name,
        String description,
        String schema
    ) {}

    /**
     * Prompt result record for display purposes.
     */
    record PromptResult(
        String agentId,
        String sessionId,
        String response,
        String toolCalls,
        TokenUsage tokenUsage
    ) {
        public PromptResult {
            if (toolCalls == null) toolCalls = "[]";
            if (tokenUsage == null) tokenUsage = new TokenUsage(0, 0, 0);
        }
    }
}
