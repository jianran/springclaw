package com.springclaw.core;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration record for an Agent.
 *
 * <p>Used during agent creation and for hot-reloading agent settings.
 * All fields except id are optional with sensible defaults.
 *
 * @param id             unique agent identifier (required)
 * @param name           human-readable display name
 * @param description    brief description of the agent's purpose
 * @param systemPrompt   system prompt that guides behavior
 * @param model          LLM model configuration
 * @param allowedTools   allowlist of tool names (empty = use all)
 * @param deniedTools    denylist of tool names (takes precedence)
 * @param memory         conversation memory configuration
 * @param metadata       arbitrary key-value metadata
 * @param createdAt      creation timestamp
 * @param updatedAt      last modification timestamp
 */
public record AgentConfig(
    String id,
    String name,
    String description,
    String systemPrompt,
    ModelConfig model,
    List<String> allowedTools,
    List<String> deniedTools,
    MemoryConfig memory,
    Map<String, Object> metadata,
    Instant createdAt,
    Instant updatedAt
) {
    public AgentConfig {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (allowedTools == null) allowedTools = Collections.emptyList();
        if (deniedTools == null) deniedTools = Collections.emptyList();
        if (metadata == null) metadata = new HashMap<>();
    }
}
