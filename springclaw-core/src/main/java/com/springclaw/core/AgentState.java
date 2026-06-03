package com.springclaw.core;

import java.time.Instant;
import java.util.List;

/**
 * Represents the runtime state of an Agent.
 *
 * <p>Provides read-only access to the agent's current conversation context,
 * token usage, and activity status.
 */
public interface AgentState {

    /** Whether the agent is currently processing a prompt. */
    boolean isRunning();

    /** ID of the current active session, or null if no session. */
    String getCurrentSessionId();

    /** Conversation transcript (recent messages). */
    List<ChatMessage> getTranscript();

    /** Cumulative token usage for this session. */
    TokenUsage getUsage();

    /** Number of turns completed in this session. */
    int getTurnCount();

    /** Timestamp of the last activity. */
    Instant getLastActivity();
}
