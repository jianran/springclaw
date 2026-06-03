package com.springclaw.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Context passed to a tool during execution.
 *
 * @param agentId   the agent invoking the tool
 * @param sessionId the active conversation session
 * @param userId    the user who initiated the request
 * @param extras    arbitrary additional context from plugins or channels
 */
public record ToolContext(
    String agentId,
    String sessionId,
    String userId,
    Map<String, Object> extras
) {
    public ToolContext {
        if (extras == null) extras = new HashMap<>();
    }
}
