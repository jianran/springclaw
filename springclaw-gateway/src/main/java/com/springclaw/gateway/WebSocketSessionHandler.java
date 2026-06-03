package com.springclaw.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springclaw.core.*;
import com.springclaw.spring.boot.AgentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

/**
 * WebSocket handler for real-time bidirectional chat.
 *
 * <p>Messages sent as JSON:
 * <pre>{@code
 * { "type": "message", "message": "Hello", "sessionId": "optional-id", "userId": "optional-id" }
 * }</pre>
 *
 * Responses streamed as JSON:
 * <pre>{@code
 * { "type": "chunk", "content": "partial response text", "sessionId": "session-id" }
 * { "type": "done", "content": "full response", "sessionId": "session-id" }
 * }</pre>
 */
public class WebSocketSessionHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AgentRegistry agentRegistry;

    public WebSocketSessionHandler(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.debug("WebSocket connected: {}", session.getId());

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(msg -> processMessage(session, msg))
                .onErrorResume(e -> {
                    log.error("WebSocket error: {}", e.getMessage());
                    return sendError(session, "Connection error: " + e.getMessage());
                })
                .doFinally(signalType -> {
                    log.debug("WebSocket disconnected: {}", session.getId());
                })
                .then(Mono.empty());
    }

    @Override
    public List<String> getSubProtocols() {
        return List.of("springclaw-v1");
    }

    private Mono<Void> processMessage(WebSocketSession session, String text) {
        try {
            JsonNode json = objectMapper.readTree(text);
            String type = json.has("type") ? json.get("type").asText() : "message";

            if ("ping".equals(type)) {
                return session.send(Flux.just(
                        session.textMessage(objectMapper.writeValueAsString(Map.of("type", "pong")))
                ));
            }

            String agentId = json.has("agentId") ? json.get("agentId").asText() : null;
            String message = json.has("message") ? json.get("message").asText() : "";
            String userId = json.has("userId") ? json.get("userId").asText() : "ws-" + session.getId();
            String sessionId = json.has("sessionId") && !json.get("sessionId").isNull()
                    ? json.get("sessionId").asText() : null;

            if (agentId == null) {
                // Use first available agent
                var agents = agentRegistry.getAllAgents();
                if (agents.isEmpty()) {
                    return session.send(Flux.just(
                            session.textMessage(objectMapper.writeValueAsString(Map.of(
                                    "type", "error",
                                    "message", "No agents registered"
                            )))
                    ));
                }
                agentId = agents.iterator().next().getId();
            }

            Agent agent = agentRegistry.getAgentOrThrow(agentId);
            String activeSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();

            SessionContext ctx = new SessionContext(
                    activeSessionId, userId, Map.of("channel", "websocket")
            );

            // Stream the response
            return agent.streamPrompt(message, ctx)
                    .doOnNext(chunk -> sendChunk(session, activeSessionId, chunk))
                    .doOnComplete(() -> sendDone(session, activeSessionId, message))
                    .then();
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", e.getMessage());
            return sendError(session, "Error: " + e.getMessage());
        }
    }

    private Mono<Void> sendChunk(WebSocketSession session, String sessionId, String content) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "chunk",
                    "sessionId", sessionId,
                    "content", content
            ));
            return session.send(Flux.just(session.textMessage(json)));
        } catch (Exception e) {
            log.warn("Failed to send chunk: {}", e.getMessage());
            return Mono.empty();
        }
    }

    private Mono<Void> sendDone(WebSocketSession session, String sessionId, String message) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "done",
                    "sessionId", sessionId,
                    "message", message
            ));
            return session.send(Flux.just(session.textMessage(json)));
        } catch (Exception e) {
            log.warn("Failed to send done: {}", e.getMessage());
            return Mono.empty();
        }
    }

    private Mono<Void> sendError(WebSocketSession session, String message) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", message
            ));
            return session.send(Flux.just(session.textMessage(json)));
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
