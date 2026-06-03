package com.springclaw.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstraction for a messaging channel (Slack, Telegram, Web, etc.).
 *
 * <p>Channels receive inbound messages from users and deliver outbound
 * responses. They are the interface between the agent runtime and
 * external communication platforms.
 *
 * @see InboundMessage
 * @see ChannelConfig
 */
public interface ChannelAdapter {

    /** Unique channel identifier. */
    String getId();

    /** Human-readable channel name. */
    String getName();

    /**
     * Initialize the channel with its configuration.
     *
     * @param config the channel configuration
     */
    void initialize(ChannelConfig config);

    /**
     * Start the channel (connect to external service).
     *
     * @return completes when the channel is ready
     */
    Mono<Void> start();

    /**
     * Stop the channel (disconnect from external service).
     *
     * @return completes when the channel is fully stopped
     */
    Mono<Void> stop();

    /** Whether the channel is currently running. */
    boolean isRunning();

    /**
     * Receive inbound messages from this channel.
     *
     * @return a Flux of inbound messages (never terminates)
     */
    Flux<InboundMessage> receive();

    /**
     * Deliver a response to a message.
     *
     * @param message the original inbound message
     * @param response the response text to deliver
     * @return completes when the message is delivered
     */
    Mono<Void> send(InboundMessage message, String response);

    /** Whether this channel supports streaming responses. */
    default boolean supportsStreaming() {
        return false;
    }
}
