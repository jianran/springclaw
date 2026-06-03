package com.springclaw.core;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A prompt request to an Agent.
 *
 * @param id            unique prompt identifier
 * @param userId        the user who sent the prompt
 * @param sessionId     the conversation session
 * @param message       the user's message text
 * @param systemPrompt  per-request system prompt override
 * @param toolNames     explicit tool names to use for this prompt
 * @param metadata      additional context
 * @param createdAt     when the prompt was created
 */
public record Prompt(
    String id,
    String userId,
    String sessionId,
    String message,
    String systemPrompt,
    List<String> toolNames,
    Map<String, Object> metadata,
    Instant createdAt
) {
    public Prompt {
        if (id == null) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
        if (toolNames == null) toolNames = Collections.emptyList();
        if (metadata == null) metadata = new HashMap<>();
    }
}
