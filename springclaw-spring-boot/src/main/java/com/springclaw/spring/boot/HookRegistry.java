package com.springclaw.spring.boot;

import com.springclaw.core.HookEvent;

/**
 * Registry for dispatching lifecycle events to registered hooks.
 */
public interface HookRegistry {

    /** Fire an event to all registered hooks. */
    void fireEvent(HookEvent event);
}
