package com.springclaw.core;

/**
 * A pre-processor that transforms a user message before it's sent to the LLM.
 *
 * <p>Pre-processors run in order of their {@code getOrder()} value.
 * They can modify the message content, inject system prompts, or enrich context.
 */
public interface PreProcessor {

    /**
     * Process the incoming message.
     *
     * @param message      the original user message
     * @param agentConfig  the agent configuration
     * @return the processed message
     */
    String process(String message, AgentConfig agentConfig);

    /** Execution order (lower values run first). */
    default int getOrder() {
        return 0;
    }
}
