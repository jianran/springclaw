package com.springclaw.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

/**
 * The primary abstraction in SpringClaw — an AI agent with configurable model,
 * tools, memory, and lifecycle hooks.
 *
 * <p>An agent wraps a Spring AI {@code ChatClient} and provides a unified API
 * for sending prompts, streaming responses, and managing conversation sessions.
 *
 * @see AgentConfig
 * @see AgentState
 * @see AgentRegistry
 */
public interface Agent {

    /** Unique identifier for this agent. */
    String getId();

    /** Human-readable name. */
    String getName();

    /** Brief description of the agent's purpose. */
    String getDescription();

    /** System prompt that guides the agent's behavior. */
    String getSystemPrompt();

    /** Configuration for the underlying LLM model. */
    ModelConfig getModelConfig();

    /** Allowed tool names (empty = use all registered tools). */
    List<String> getAllowedTools();

    /** Denied tool names (these are excluded even if allowed). */
    List<String> getDeniedTools();

    /** Current runtime state of this agent. */
    AgentState getState();

    /**
     * Send a prompt and get a complete response.
     *
     * @param message the user's message
     * @param context the session context
     * @return the prompt result with response text and metadata
     */
    PromptResult prompt(String message, SessionContext context);

    /**
     * Send a structured prompt request.
     *
     * @param request the prompt request
     * @return the prompt result
     */
    PromptResult prompt(Prompt request);

    /**
     * Send a prompt and stream the response token-by-token.
     *
     * @param message the user's message
     * @param context the session context
     * @return a Flux of response text chunks
     */
    Flux<String> streamPrompt(String message, SessionContext context);

    /** Returns all tools registered for this agent. */
    Collection<ToolDefinition> getRegisteredTools();

    /** Register a new tool for this agent. */
    void registerTool(ToolDefinition tool);

    /** Unregister a tool by name. */
    void unregisterTool(String toolName);

    /** Reset the conversation history for a session. */
    void resetSession(String sessionId);

    /** Export the agent's current configuration. */
    AgentConfig toConfig();
}
