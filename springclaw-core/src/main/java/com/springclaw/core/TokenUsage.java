package com.springclaw.core;

/**
 * Tracks token consumption for an LLM interaction.
 *
 * @param promptTokens     tokens in the input prompt
 * @param completionTokens tokens in the model's output
 * @param totalTokens      total tokens consumed
 */
public record TokenUsage(
    int promptTokens,
    int completionTokens,
    int totalTokens
) {
    public TokenUsage {
        if (promptTokens < 0) promptTokens = 0;
        if (completionTokens < 0) completionTokens = 0;
        if (totalTokens < 0) totalTokens = 0;
    }
}
