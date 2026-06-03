package com.springclaw.core;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of processing a Prompt.
 *
 * @param agentId       the agent that processed the prompt
 * @param sessionId     the conversation session
 * @param response      the assistant's response text
 * @param toolCalls     tool calls made during this turn
 * @param tokenUsage    token consumption
 * @param metadata      additional result metadata
 * @param createdAt     when the result was created
 */
public record PromptResult(
    String agentId,
    String sessionId,
    String response,
    List<ToolCall> toolCalls,
    TokenUsage tokenUsage,
    Map<String, Object> metadata,
    Instant createdAt
) {
    public PromptResult {
        if (createdAt == null) createdAt = Instant.now();
        if (toolCalls == null) toolCalls = Collections.emptyList();
        if (metadata == null) metadata = new HashMap<>();
        if (tokenUsage == null) tokenUsage = new TokenUsage(0, 0, 0);
    }
}
