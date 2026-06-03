package com.springclaw.gateway;

import com.springclaw.core.*;
import com.springclaw.spring.boot.GatewayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.WebHandler;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * WebFlux-based gateway server implementation.
 *
 * <p>Starts a Netty server on the configured port and registers:
 * <ul>
 *   <li>REST API routes at /api/agents/**</li>
 *   <li>WebSocket endpoint at /ws</li>
 *   <li>Static files (chat widget) at /</li>
 * </ul>
 */
public class GatewayServerImpl implements GatewayServer {

    private static final Logger log = LoggerFactory.getLogger(GatewayServerImpl.class);

    private final GatewayProperties properties;
    private final ChannelManager channelManager;
    private volatile boolean running = false;
    private volatile int actualPort = 0;

    public GatewayServerImpl(GatewayProperties properties, ChannelManager channelManager) {
        this.properties = properties;
        this.channelManager = channelManager;
    }

    @Override
    public Mono<Void> start() {
        return Mono.fromRunnable(() -> {
                    log.info("Starting SpringClaw Gateway on {}:{}...", properties.getBind(), properties.getPort());
                })
                .then(Mono.defer(() -> {
                    // Start channels
                    return channelManager.startAll()
                            .doOnSuccess(v -> log.info("All channels started"))
                            .then(Mono.defer(() -> {
                                // Start HTTP server
                                WebHandler webHandler = RouterFunctions.toWebHandler(
                                                RouterFunctions.route().build()
                                        );
                                org.springframework.http.server.reactive.HttpHandler httpHandler = WebHttpHandlerBuilder
                                        .webHandler(webHandler)
                                        .build();

                                ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);

                                return Mono.create(sink -> {
                                    HttpServer server = HttpServer.create()
                                            .handle(adapter)
                                            .bindAddress(() -> new java.net.InetSocketAddress(properties.getBind(), properties.getPort()));

                                    server.bind().doOnSuccess(bind -> {
                                        actualPort = bind.port();
                                        running = true;
                                        log.info("SpringClaw Gateway started on port {}", actualPort);
                                        sink.success();
                                    }).doOnError(sink::error);
                                }).then();
                            }));
                }))
                .doOnError(e -> log.error("Failed to start gateway: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> stop() {
        return Mono.fromRunnable(() -> {
                    log.info("Stopping SpringClaw Gateway...");
                })
                .then(channelManager.stopAll())
                .doOnSuccess(v -> {
                    running = false;
                    log.info("SpringClaw Gateway stopped");
                });
    }

    @Override
    public int getPort() {
        return actualPort > 0 ? actualPort : properties.getPort();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
