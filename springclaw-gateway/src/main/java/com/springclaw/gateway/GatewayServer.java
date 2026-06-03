package com.springclaw.gateway;

import reactor.core.publisher.Mono;

/**
 * Main gateway server that manages channels and routes messages.
 *
 * <p>The gateway is the entry point for external communication. It starts
 * a WebFlux server, registers channel adapters, and routes inbound messages
 * to the appropriate agent.
 */
public interface GatewayServer {

    /** Start the gateway server. */
    Mono<Void> start();

    /** Stop the gateway server gracefully. */
    Mono<Void> stop();

    /** The port the server is listening on. */
    int getPort();

    /** Whether the server is currently running. */
    boolean isRunning();
}
