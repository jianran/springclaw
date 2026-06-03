package com.springclaw.gateway;

import com.springclaw.channels.WebChannelAdapter;
import com.springclaw.core.ChannelAdapter;
import com.springclaw.core.ChannelConfig;
import com.springclaw.core.InboundMessage;
import com.springclaw.spring.boot.AgentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages channel lifecycle and message routing.
 *
 * <p>Registers channels, starts/stops them, and routes inbound messages
 * to the appropriate agent via the AgentRegistry.
 */
public class ChannelManager {

    private static final Logger log = LoggerFactory.getLogger(ChannelManager.class);

    private final AgentRegistry agentRegistry;
    private final Map<String, ChannelAdapter> channels = new ConcurrentHashMap<>();
    private final Sinks.Many<InboundMessage> inboundSink = Sinks.many().multicast().onBackpressureBuffer();

    public ChannelManager(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    /**
     * Register a channel adapter.
     */
    public void registerChannel(ChannelAdapter adapter) {
        registerChannel(adapter, new ChannelConfig(adapter.getId(), Map.of()));
    }

    /**
     * Register a channel with custom configuration.
     */
    public void registerChannel(ChannelAdapter adapter, ChannelConfig config) {
        channels.put(adapter.getId(), adapter);
        log.info("Registered channel: {} ({})", adapter.getId(), adapter.getName());
    }

    /**
     * Start all registered channels.
     */
    public Mono<Void> startAll() {
        return Flux.fromIterable(channels.values())
                .flatMap(adapter -> adapter.start())
                .then();
    }

    /**
     * Stop all registered channels.
     */
    public Mono<Void> stopAll() {
        return Flux.fromIterable(channels.values())
                .flatMap(ChannelAdapter::stop)
                .then();
    }

    /**
     * Receive all inbound messages from all channels.
     */
    public Flux<InboundMessage> receiveAll() {
        return inboundSink.asFlux();
    }

    /**
     * Handle an inbound message by routing it to the appropriate agent.
     */
    public Mono<String> handleInbound(InboundMessage message) {
        log.debug("Handling inbound message from channel '{}' for session '{}'",
                message.channelId(), message.sessionId());

        // Subscribe channel receivers
        subscribeChannelReceiver(message.channelId());

        // Route to agent
        // Default: use the first available agent or the one specified in session metadata
        String agentId = resolveAgentId(message);
        var agent = agentRegistry.getAgentOrThrow(agentId);

        // Get or create session ID
        String sessionId = message.sessionId() != null ? message.sessionId()
                : createSessionId(message);

        com.springclaw.core.SessionContext ctx = new com.springclaw.core.SessionContext(
                sessionId, message.userId(), message.metadata()
        );

        // Send prompt
        var result = agent.prompt(message.content(), ctx);
        return Mono.just(result.response());
    }

    private void subscribeChannelReceiver(String channelId) {
        ChannelAdapter adapter = channels.get(channelId);
        if (adapter == null) return;

        adapter.receive()
                .subscribe(
                        msg -> inboundSink.tryEmitNext(msg),
                        err -> log.error("Channel '{}' error: {}", channelId, err.getMessage()),
                        () -> log.warn("Channel '{}' terminated", channelId)
                );
    }

    private String resolveAgentId(InboundMessage message) {
        // Check metadata for explicit agent override
        Object agentIdObj = message.metadata().get("agentId");
        if (agentIdObj instanceof String agentId) {
            return agentId;
        }

        // Default: use first registered agent
        var agents = agentRegistry.getAllAgents();
        if (agents.isEmpty()) {
            throw new IllegalStateException("No agents registered");
        }
        return agents.iterator().next().getId();
    }

    private String createSessionId(InboundMessage message) {
        // Create session key like OpenClaw: channel:chatType:id
        String chatType = (String) message.metadata().getOrDefault("chatType", "direct");
        return message.channelId() + ":" + chatType + ":" + message.userId();
    }

    /**
     * Get all registered channels.
     */
    public Collection<ChannelAdapter> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }
}
