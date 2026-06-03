package com.springclaw.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request to execute an agent run via a harness.
 *
 * @param agentId           the agent ID
 * @param message           the user message
 * @param systemPrompt      the system prompt
 * @param tools             available tools for this run
 * @param conversationHistory the conversation history (recent messages)
 * @param runningUsage      cumulative token usage
 * @param metadata          additional execution metadata
 */
public record HarnessRequest(
    String agentId,
    String message,
    String systemPrompt,
    List<ToolDefinition> tools,
    List<ChatMessage> conversationHistory,
    TokenUsage runningUsage,
    Map<String, Object> metadata
) {
    public HarnessRequest {
        if (tools == null) tools = Collections.emptyList();
        if (conversationHistory == null) conversationHistory = Collections.emptyList();
        if (metadata == null) metadata = new HashMap<>();
    }
}
