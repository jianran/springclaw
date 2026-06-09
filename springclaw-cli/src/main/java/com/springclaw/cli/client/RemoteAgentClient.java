package com.springclaw.cli.client;

import com.springclaw.cli.client.AgentClient.AgentInfo;
import com.springclaw.cli.client.AgentClient.ToolDefinition;
import com.springclaw.core.TokenUsage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agent client implementation for remote mode.
 *
 * <p>Connects to a running SpringClaw gateway via HTTP/WebSocket.
 * Uses the gateway's REST API for prompts and agent management.
 */
public class RemoteAgentClient implements AgentClient {

    private static final Logger log = LoggerFactory.getLogger(RemoteAgentClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String gatewayUrl;

    public RemoteAgentClient(String gatewayUrl, ObjectMapper objectMapper) {
        this.gatewayUrl = gatewayUrl;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(gatewayUrl)
                .build();
    }

    @Override
    public AgentInfo getAgentInfo(String agentId) {
        try {
            JsonNode node = webClient.get()
                    .uri("/api/agents/{id}", agentId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (node == null || !node.has("id")) return null;

            return new AgentInfo(
                    node.get("id").asText(),
                    node.get("name").asText(),
                    node.has("description") ? node.get("description").asText() : "",
                    node.has("systemPrompt") ? node.get("systemPrompt").asText() : ""
            );
        } catch (Exception e) {
            log.warn("Failed to get agent info for '{}': {}", agentId, e.getMessage());
            return null;
        }
    }

    @Override
    public Collection<AgentInfo> listAgents() {
        try {
            JsonNode node = webClient.get()
                    .uri("/api/agents")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (node == null || !node.isArray()) return Collections.emptyList();

            List<AgentInfo> agents = new ArrayList<>();
            for (JsonNode agentNode : node) {
                agents.add(new AgentInfo(
                        agentNode.get("id").asText(),
                        agentNode.get("name").asText(),
                        agentNode.has("description") ? agentNode.get("description").asText() : "",
                        agentNode.has("systemPrompt") ? agentNode.get("systemPrompt").asText() : ""
                ));
            }
            return agents;
        } catch (Exception e) {
            log.warn("Failed to list agents: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public PromptResult prompt(String agentId, String message, String sessionId) {
        try {
            JsonNode request = objectMapper.createObjectNode()
                    .put("message", message)
                    .put("sessionId", sessionId != null ? sessionId : "");

            JsonNode response = webClient.post()
                    .uri("/api/agents/{id}/prompt", agentId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                return new PromptResult(agentId, sessionId, "", "[]", new TokenUsage(0, 0, 0));
            }

            String responseText = response.has("response") ? response.get("response").asText() : "";
            String responseSessionId = response.has("sessionId") ? response.get("sessionId").asText() : sessionId;
            String toolCallsJson = response.has("toolCalls") ? response.get("toolCalls").toString() : "[]";
            TokenUsage tokenUsage = parseTokenUsage(response);

            return new PromptResult(agentId, responseSessionId, responseText, toolCallsJson, tokenUsage);
        } catch (Exception e) {
            throw new RuntimeException("Remote prompt failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> streamPrompt(String agentId, String message, String sessionId) {
        String effectiveSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();

        return webClient.post()
                .uri("/api/agents/{id}/prompt/stream", agentId)
                .bodyValue(objectMapper.createObjectNode()
                        .put("message", message)
                        .put("sessionId", effectiveSessionId))
                .retrieve()
                .bodyToFlux(String.class)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public void resetSession(String agentId, String sessionId) {
        try {
            webClient.delete()
                    .uri("/api/agents/{id}/session/{sessionId}", agentId, sessionId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.warn("Failed to reset session '{}': {}", sessionId, e.getMessage());
        }
    }

    @Override
    public Collection<ToolDefinition> listTools(String agentId) {
        try {
            JsonNode node = webClient.get()
                    .uri("/api/agents/{id}/tools", agentId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (node == null || !node.isArray()) return Collections.emptyList();

            List<ToolDefinition> tools = new ArrayList<>();
            for (JsonNode toolNode : node) {
                tools.add(new ToolDefinition(
                        toolNode.get("name").asText(),
                        toolNode.has("description") ? toolNode.get("description").asText() : "",
                        toolNode.has("schema") ? toolNode.get("schema").asText() : ""
                ));
            }
            return tools;
        } catch (Exception e) {
            log.warn("Failed to list tools for '{}': {}", agentId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private TokenUsage parseTokenUsage(JsonNode response) {
        if (response.has("tokenUsage")) {
            JsonNode usage = response.get("tokenUsage");
            int prompt = usage.has("promptTokens") ? usage.get("promptTokens").asInt() : 0;
            int completion = usage.has("completionTokens") ? usage.get("completionTokens").asInt() : 0;
            int total = usage.has("totalTokens") ? usage.get("totalTokens").asInt() : 0;
            return new TokenUsage(prompt, completion, total);
        }
        return new TokenUsage(0, 0, 0);
    }
}
