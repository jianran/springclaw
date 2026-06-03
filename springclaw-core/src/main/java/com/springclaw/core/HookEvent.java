package com.springclaw.core;

import java.time.Instant;

/**
 * An event emitted by the agent lifecycle for hook consumers.
 *
 * @param type      the event type (session, prompt, tool, error)
 * @param agentId   the agent that generated the event
 * @param sessionId the conversation session
 * @param userId    the user involved in the event
 * @param payload   event-specific data (type depends on EventType)
 * @param timestamp when the event occurred
 */
public record HookEvent(
    EventType type,
    String agentId,
    String sessionId,
    String userId,
    Object payload,
    Instant timestamp
) {
    public HookEvent {
        if (timestamp == null) timestamp = Instant.now();
    }
}
