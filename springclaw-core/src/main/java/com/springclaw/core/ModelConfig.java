package com.springclaw.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for the underlying LLM model.
 *
 * @param provider      provider identifier (e.g., "openai", "anthropic", "ollama")
 * @param modelId       model identifier (e.g., "gpt-4o", "claude-3-5-sonnet")
 * @param temperature   sampling temperature (0.0 - 2.0)
 * @param maxTokens     maximum response tokens
 * @param extraOptions  provider-specific options
 */
public record ModelConfig(
    String provider,
    String modelId,
    Double temperature,
    Integer maxTokens,
    Map<String, Object> extraOptions
) {
    public ModelConfig {
        if (extraOptions == null) extraOptions = new HashMap<>();
    }
}
