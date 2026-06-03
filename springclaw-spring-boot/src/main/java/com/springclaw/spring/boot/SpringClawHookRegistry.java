package com.springclaw.spring.boot;

import com.springclaw.core.Hook;
import com.springclaw.core.HookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages hook registration and event dispatch.
 *
 * <p>Forwards events to registered hooks, supporting both synchronous and
 * asynchronous execution based on each hook's {@code isAsync()} setting.
 */
public class SpringClawHookRegistry implements HookRegistry {

    private static final Logger log = LoggerFactory.getLogger(SpringClawHookRegistry.class);

    private final List<Hook> hooks;
    private final boolean enabled;
    private final ExecutorService asyncExecutor;

    public SpringClawHookRegistry(List<Hook> hooks, boolean enabled) {
        this.hooks = hooks;
        this.enabled = enabled;
        long asyncCount = hooks.stream().filter(Hook::isAsync).count();
        this.asyncExecutor = asyncCount > 0 ? Executors.newFixedThreadPool((int) asyncCount) : null;
        log.info("SpringClawHookRegistry initialized with {} hooks (enabled={}, async={})",
                hooks.size(), enabled, asyncCount);
    }

    @Override
    public void fireEvent(HookEvent event) {
        if (!enabled || hooks.isEmpty()) {
            return;
        }

        for (Hook hook : hooks) {
            try {
                if (hook.isAsync() && asyncExecutor != null) {
                    asyncExecutor.execute(() -> dispatch(hook, event));
                } else {
                    dispatch(hook, event);
                }
            } catch (Exception e) {
                log.warn("Hook '{}' threw exception for event {}: {}",
                        hook.getName(), event.type(), e.getMessage());
            }
        }
    }

    private void dispatch(Hook hook, HookEvent event) {
        if (event.sessionId() == null) {
            return; // Skip events without session context
        }
        hook.onEvent(event);
    }

    /**
     * Shutdown the async executor. Called during application shutdown.
     */
    public void shutdown() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
