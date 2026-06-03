package com.springclaw.core;

/**
 * A post-processor that transforms an LLM response before it's delivered to the user.
 *
 * <p>Post-processors run in order of their {@code getOrder()} value.
 * They can modify the response, add signatures, or format output.
 */
public interface PostProcessor {

    /**
     * Process the LLM response.
     *
     * @param response   the original LLM response
     * @param result     the full prompt result with metadata
     * @return the processed response
     */
    String process(String response, PromptResult result);

    /** Execution order (lower values run first). */
    default int getOrder() {
        return 0;
    }
}
