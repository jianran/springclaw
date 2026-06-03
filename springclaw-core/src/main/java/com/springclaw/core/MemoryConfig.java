package com.springclaw.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for conversation memory.
 *
 * @param type         memory backend type: "in-memory", "redis", "jdbc", "mongo"
 * @param maxMessages  maximum messages to retain in conversation history
 * @param extraConfig  backend-specific configuration
 */
public record MemoryConfig(
    String type,
    int maxMessages,
    Map<String, Object> extraConfig
) {
    public MemoryConfig {
        if (maxMessages <= 0) maxMessages = 50;
        if (extraConfig == null) extraConfig = new HashMap<>();
    }
}
