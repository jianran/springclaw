package com.springclaw.gateway;

import com.springclaw.core.*;
import com.springclaw.spring.boot.AgentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

/**
 * REST API controller for interacting with agents.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/agents/{agentId}/prompt — send message (non-streaming)</li>
 *   <li>POST /api/agents/{agentId}/prompt/stream — send message (streaming SSE)</li>
 *   <li>GET /api/agents/{agentId}/sessions/{sessionId}/messages — get conversation</li>
 *   <li>GET /api/agents/{agentId} — get agent info</li>
 *   <li>GET /api/agents — list all agents</li>
 *   <li>DELETE /api/agents/{agentId}/sessions/{sessionId} — reset session</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/agents")
public class GatewayController {

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    private final AgentRegistry agentRegistry;

    public GatewayController(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    /**
     * Send a prompt and get a complete response.
     */
    @PostMapping("/{agentId}/prompt")
    public Mono<ResponseEntity<PromptResponse>> prompt(
            @PathVariable String agentId,
            @RequestBody(required = false) PromptRequest request
    ) {
        PromptRequest finalRequest = request != null ? request : new PromptRequest();

        return Mono.fromCallable(() -> {
            Agent agent = agentRegistry.getAgentOrThrow(agentId);
            String sessionId = finalRequest.sessionId() != null ? finalRequest.sessionId()
                    : UUID.randomUUID().toString();

            SessionContext ctx = new SessionContext(
                    sessionId,
                    finalRequest.userId() != null ? finalRequest.userId() : "anonymous",
                    finalRequest.metadata() != null ? finalRequest.metadata() : Map.of()
            );

            PromptResult result = agent.prompt(finalRequest.message(), ctx);
            return ResponseEntity.ok(new PromptResponse(
                    result.agentId(),
                    result.sessionId(),
                    result.response(),
                    result.toolCalls().isEmpty() ? null
                            : result.toolCalls().stream().map(tc -> new ToolCallResponse(tc.toolName(), tc.arguments())).toList(),
                    result.tokenUsage(),
                    result.createdAt()
            ));
        }).onErrorResume(IllegalArgumentException.class, e ->
                Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new PromptResponse(agentId, null, "Agent not found: " + e.getMessage(), null, null, Instant.now())))
        ).onErrorResume(e ->
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new PromptResponse(agentId, null, "Error: " + e.getMessage(), null, null, Instant.now())))
        );
    }

    /**
     * Send a prompt and stream the response as SSE.
     */
    @PostMapping("/{agentId}/prompt/stream")
    public Flux<String> promptStream(
            @PathVariable String agentId,
            @RequestBody(required = false) PromptRequest request
    ) {
        if (request == null) request = new PromptRequest();

        Agent agent = agentRegistry.getAgentOrThrow(agentId);
        String sessionId = request.sessionId() != null ? request.sessionId()
                : UUID.randomUUID().toString();

        SessionContext ctx = new SessionContext(
                sessionId,
                request.userId() != null ? request.userId() : "anonymous",
                request.metadata() != null ? request.metadata() : Map.of()
        );

        // Format as SSE
        return agent.streamPrompt(request.message(), ctx)
                .map(chunk -> "data: " + chunk + "\n\n")
                .concatWith(Mono.just("data: [DONE]\n\n"));
    }

    /**
     * Get agent information.
     */
    @GetMapping("/{agentId}")
    public ResponseEntity<AgentInfo> getAgent(@PathVariable String agentId) {
        Agent agent = agentRegistry.getAgentOrThrow(agentId);
        AgentInfo info = new AgentInfo(
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getSystemPrompt(),
                agent.getModelConfig()
        );
        return ResponseEntity.ok(info);
    }

    /**
     * List all agents.
     */
    @GetMapping
    public ResponseEntity<List<AgentInfo>> listAgents() {
        List<AgentInfo> agents = agentRegistry.getAllAgents().stream()
                .map(a -> new AgentInfo(
                        a.getId(), a.getName(), a.getDescription(),
                        a.getSystemPrompt(), a.getModelConfig()
                ))
                .toList();
        return ResponseEntity.ok(agents);
    }

    /**
     * Get conversation history for a session.
     */
    @GetMapping("/{agentId}/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        Agent agent = agentRegistry.getAgentOrThrow(agentId);
        AgentState state = agent.getState();
        List<ChatMessage> messages = state.getTranscript();

        // If no transcript in state, try to get from session directly
        if (messages == null || messages.isEmpty()) {
            messages = List.of();
        }

        int from = Math.max(0, messages.size() - limit);
        List<ChatMessageResponse> result = messages.subList(from, messages.size()).stream()
                .map(m -> new ChatMessageResponse(
                        m.id(), m.role(), m.content(), m.toolCallId(),
                        m.toolName(), m.timestamp()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * Reset a session.
     */
    @DeleteMapping("/{agentId}/sessions/{sessionId}")
    public ResponseEntity<Void> resetSession(
            @PathVariable String agentId,
            @PathVariable String sessionId
    ) {
        Agent agent = agentRegistry.getAgentOrThrow(agentId);
        agent.resetSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Request body for prompt endpoints.
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
     * Response for prompt endpoints.
     */
    public record PromptResponse(
            String agentId,
            String sessionId,
            String response,
            List<ToolCallResponse> toolCalls,
            TokenUsage tokenUsage,
            Instant createdAt
    ) {}

    /**
     * Response for tool call in prompt.
     */
    public record ToolCallResponse(
            String toolName,
            Map<String, Object> arguments
    ) {}

    /**
     * Agent information response.
     */
    public record AgentInfo(
            String id,
            String name,
            String description,
            String systemPrompt,
            ModelConfig model
    ) {}

    /**
     * Chat message response.
     */
    public record ChatMessageResponse(
            String id,
            Role role,
            String content,
            String toolCallId,
            String toolName,
            Instant timestamp
    ) {}
}
