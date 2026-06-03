package com.springclaw.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A tool invocation request from the LLM.
 *
 * @param id          unique tool call identifier
 * @param toolName    name of the tool to invoke
 * @param arguments   parameters for the tool call
 * @param requestedAt when the call was requested
 * @param completedAt when the call completed (null if pending)
 */
public record ToolCall(
    String id,
    String toolName,
    Map<String, Object> arguments,
    Instant requestedAt,
    Instant completedAt
) {
    public ToolCall {
        if (id == null) id = UUID.randomUUID().toString();
        if (requestedAt == null) requestedAt = Instant.now();
        if (arguments == null) arguments = new HashMap<>();
    }
}
