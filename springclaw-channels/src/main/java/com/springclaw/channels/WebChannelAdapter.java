package com.springclaw.channels;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springclaw.core.*;
import com.springclaw.spring.boot.AgentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Built-in web channel adapter.
 *
 * <p>Provides REST API and WebSocket endpoints for the chat widget.
 * Supports both streaming and non-streaming interactions.
 */
public class WebChannelAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebChannelAdapter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AgentRegistry agentRegistry;
    private final Sinks.Many<InboundMessage> inboundSink = Sinks.many().multicast().onBackpressureBuffer();
    private volatile boolean isRunning = false;
    private ChannelConfig config;

    public WebChannelAdapter(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    @Override
    public String getId() {
        return "web";
    }

    @Override
    public String getName() {
        return "Web Channel";
    }

    @Override
    public void initialize(ChannelConfig config) {
        this.config = config;
        log.info("Web channel initialized");
    }

    @Override
    public Mono<Void> start() {
        return Mono.fromRunnable(() -> {
            isRunning = true;
            log.info("Web channel started");
        });
    }

    @Override
    public Mono<Void> stop() {
        return Mono.fromRunnable(() -> {
            isRunning = false;
            log.info("Web channel stopped");
        });
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public Flux<InboundMessage> receive() {
        return inboundSink.asFlux();
    }

    @Override
    public Mono<Void> send(InboundMessage message, String response) {
        log.debug("Sending response to channel '{}' for message '{}'", message.channelId(), message.id());
        // Web channel delivers responses through REST/WebSocket, not through this method
        return Mono.empty();
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    /**
     * Handle a REST API prompt. Called by GatewayController.
     */
    public Mono<PromptResponse> handlePrompt(String agentId, PromptRequest request) {
        if (request == null) request = new PromptRequest();

        Agent agent = agentRegistry.getAgentOrThrow(agentId);
        String sessionId = request.sessionId() != null ? request.sessionId()
                : UUID.randomUUID().toString();

        SessionContext ctx = new SessionContext(
                sessionId,
                request.userId() != null ? request.userId() : "web-" + sessionId,
                request.metadata() != null ? request.metadata() : Map.of("channel", "web")
        );

        PromptResult result = agent.prompt(request.message(), ctx);

        return Mono.just(new PromptResponse(
                result.agentId(),
                result.sessionId(),
                result.response(),
                result.toolCalls().isEmpty() ? null
                        : result.toolCalls().stream().map(tc -> new ToolCallInfo(tc.toolName(), tc.arguments())).toList(),
                result.tokenUsage(),
                result.createdAt()
        ));
    }

    /**
     * Handle a streaming prompt. Called by GatewayController.
     */
    public Flux<String> handleStream(String agentId, PromptRequest request) {
        if (request == null) request = new PromptRequest();

        Agent agent = agentRegistry.getAgentOrThrow(agentId);
        String sessionId = request.sessionId() != null ? request.sessionId()
                : UUID.randomUUID().toString();

        SessionContext ctx = new SessionContext(
                sessionId,
                request.userId() != null ? request.userId() : "web-" + sessionId,
                request.metadata() != null ? request.metadata() : Map.of("channel", "web")
        );

        return agent.streamPrompt(request.message(), ctx)
                .map(chunk -> "data: " + chunk + "\n\n")
                .concatWith(Mono.just("data: [DONE]\n\n"));
    }

    /**
     * Process an inbound message from the REST API and emit to the channel sink.
     */
    public Mono<InboundMessage> processInbound(String agentId, PromptRequest request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();
        String userId = request.userId() != null ? request.userId() : "web-" + sessionId;

        InboundMessage message = new InboundMessage(
                null, "web", sessionId, userId,
                "Web User", request.message(),
                request.metadata() != null ? request.metadata() : Map.of("channel", "web"),
                Instant.now()
        );

        inboundSink.tryEmitNext(message);

        // Process and return response
        Agent agent = agentRegistry.getAgentOrThrow(agentId);
        SessionContext ctx = new SessionContext(sessionId, userId, Map.of("channel", "web"));
        PromptResult result = agent.prompt(request.message(), ctx);

        // Deliver response as outbound
        return Mono.just(message).map(m -> {
            // The response is delivered through the REST response, not this channel
            return m;
        });
    }

    /**
     * Request body for web channel prompts.
     */
    public record PromptRequest(
            String message,
            String userId,
            String sessionId,
            Map<String, Object> metadata
    ) {
        public PromptRequest {
            if (metadata == null) metadata = new HashMap<>();
        }

        public PromptRequest() {
            this("", null, null, new HashMap<>());
        }
    }

    /**
     * Response for web channel prompts.
     */
    public record PromptResponse(
            String agentId,
            String sessionId,
            String response,
            List<ToolCallInfo> toolCalls,
            TokenUsage tokenUsage,
            Instant createdAt
    ) {}

    /**
     * Tool call info in response.
     */
    public record ToolCallInfo(
            String toolName,
            Map<String, Object> arguments
    ) {}
}
