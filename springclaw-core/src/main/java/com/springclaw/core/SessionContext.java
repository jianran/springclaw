package com.springclaw.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Context for a session/prompt.
 *
 * @param sessionId  the conversation session identifier
 * @param userId     the user identifier
 * @param metadata   additional context (e.g., channel info, user preferences)
 */
public record SessionContext(
    String sessionId,
    String userId,
    Map<String, Object> metadata
) {
    public SessionContext {
        if (metadata == null) metadata = new HashMap<>();
    }
}
