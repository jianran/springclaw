package com.springclaw.core;

import reactor.core.publisher.Mono;

/**
 * A background service managed by the plugin system.
 *
 * <p>Services have a lifecycle (start/stop) and can run background tasks
 * such as cron scheduling, health monitoring, or periodic cleanup.
 */
public interface Service {

    /** Unique service identifier. */
    String getId();

    /** Start the service. */
    Mono<Void> start();

    /** Stop the service. */
    Mono<Void> stop();
}
