package com.springclaw.core;

/**
 * Context for harness selection.
 *
 * @param modelConfig         the model configuration
 * @param provider            the model provider identifier
 * @param streamingSupported  whether the provider supports streaming
 */
public record HarnessContext(
    ModelConfig modelConfig,
    String provider,
    boolean streamingSupported
) {}
