package com.springclaw.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Pluggable execution backend for an Agent.
 *
 * <p>The harness abstraction allows swapping the agent runtime without
 * changing the Agent interface. Default implementations use Spring AI's
 * ChatClient; custom harnesses can use different transports or models.
 *
 * @see HarnessContext
 * @see HarnessRequest
 */
public interface AgentHarness {

    /** Unique harness identifier (e.g., "spring-ai", "custom"). */
    String getId();

    /**
     * Check if this harness supports the given context.
     *
     * @param context the model/provider context
     * @return true if this harness can handle the context
     */
    boolean supports(HarnessContext context);

    /**
     * Execute a non-streaming agent run.
     *
     * @param request the execution request
     * @return the prompt result
     */
    Mono<PromptResult> execute(HarnessRequest request);

    /**
     * Execute a streaming agent run.
     *
     * @param request the execution request
     * @return flux of response text chunks
     */
    Flux<String> executeStreaming(HarnessRequest request);

    /** Clean up resources held by this harness. */
    default void dispose() {}
}
