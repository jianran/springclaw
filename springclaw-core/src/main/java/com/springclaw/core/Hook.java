package com.springclaw.core;

/**
 * A lifecycle hook that intercepts agent events.
 *
 * <p>Hooks allow plugins and observability tools to react to agent lifecycle
 * events such as session start, tool calls, and response completion.
 *
 * <p>Implementations can be synchronous (default) or asynchronous.
 *
 * @see HookEvent
 * @see EventType
 */
public interface Hook {

    /** Unique name for this hook (used for identification and ordering). */
    String getName();

    /**
     * Handle a hook event.
     *
     * @param event the event to handle
     */
    void onEvent(HookEvent event);

    /**
     * Whether this hook should be executed asynchronously.
     * Async hooks are dispatched on a separate thread pool.
     *
     * @return true if async
     */
    default boolean isAsync() {
        return false;
    }
}
