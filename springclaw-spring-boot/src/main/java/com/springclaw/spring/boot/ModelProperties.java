package com.springclaw.spring.boot;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM model configuration.
 */
public class ModelProperties {

    /** Provider identifier (e.g., "openai", "anthropic", "ollama"). */
    private String provider = "openai";

    /** Model identifier (e.g., "gpt-4o", "claude-3-5-sonnet"). */
    private String modelId = "gpt-4o";

    /** Sampling temperature (0.0 - 2.0). */
    private Double temperature;

    /** Maximum response tokens. */
    private Integer maxTokens;

    /** Provider-specific options (temperature override, top-p, etc.). */
    private Map<String, Object> extraOptions = new HashMap<>();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Map<String, Object> getExtraOptions() {
        return extraOptions;
    }

    public void setExtraOptions(Map<String, Object> extraOptions) {
        this.extraOptions = extraOptions;
    }
}
