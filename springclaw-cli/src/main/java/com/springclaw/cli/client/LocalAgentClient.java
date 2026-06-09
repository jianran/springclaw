package com.springclaw.cli.client;

import com.springclaw.cli.client.AgentClient.AgentInfo;
import com.springclaw.cli.client.AgentClient.ToolDefinition;
import com.springclaw.core.*;
import com.springclaw.spring.boot.AgentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent client implementation for local mode.
 *
 * <p>Wraps the SpringClaw {@link AgentRegistry} directly, providing
 * in-process access to agents without any HTTP layer.
 */
public class LocalAgentClient implements AgentClient {

    private static final Logger log = LoggerFactory.getLogger(LocalAgentClient.class);

    private final AgentRegistry agentRegistry;

    public LocalAgentClient(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    @Override
    public AgentInfo getAgentInfo(String agentId) {
        com.springclaw.core.Agent agent = agentRegistry.getAgent(agentId);
        if (agent == null) return null;
        return toAgentInfo(agent);
    }

    @Override
    public Collection<AgentInfo> listAgents() {
        return agentRegistry.getAllAgents().stream()
                .map(this::toAgentInfo)
                .collect(Collectors.toList());
    }

    @Override
    public PromptResult prompt(String agentId, String message, String sessionId) {
        com.springclaw.core.Agent agent = agentRegistry.getAgent(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found: " + agentId);
        }

        SessionContext ctx = new SessionContext(
                sessionId, "cli",
                Map.of("agentId", agentId, "sessionId", sessionId));

        com.springclaw.core.PromptResult result = agent.prompt(message, ctx);
        return toPromptResult(result);
    }

    @Override
    public Flux<String> streamPrompt(String agentId, String message, String sessionId) {
        com.springclaw.core.Agent agent = agentRegistry.getAgent(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found: " + agentId);
        }

        SessionContext ctx = new SessionContext(
                sessionId, "cli",
                Map.of("agentId", agentId, "sessionId", sessionId));

        return agent.streamPrompt(message, ctx);
    }

    @Override
    public void resetSession(String agentId, String sessionId) {
        com.springclaw.core.Agent agent = agentRegistry.getAgent(agentId);
        if (agent != null) {
            agent.resetSession(sessionId);
            log.debug("Reset session '{}' for agent '{}'", sessionId, agentId);
        }
    }

    @Override
    public Collection<ToolDefinition> listTools(String agentId) {
        com.springclaw.core.Agent agent = agentRegistry.getAgent(agentId);
        if (agent == null) return Collections.emptyList();

        return agent.getRegisteredTools().stream()
                .map(t -> new ToolDefinition(t.getName(), t.getDescription(), t.getSchema()))
                .collect(Collectors.toList());
    }

    private AgentInfo toAgentInfo(com.springclaw.core.Agent agent) {
        return new AgentInfo(
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getSystemPrompt()
        );
    }

    private PromptResult toPromptResult(com.springclaw.core.PromptResult result) {
        String toolCallsJson = "[]";
        if (result.toolCalls() != null && !result.toolCalls().isEmpty()) {
            toolCallsJson = result.toolCalls().stream()
                    .map(tc -> String.format("{\"id\":\"%s\",\"tool\":\"%s\"}",
                            tc.id(), tc.toolName()))
                    .collect(Collectors.joining(",", "[", "]"));
        }

        return new PromptResult(
                result.agentId(),
                result.sessionId(),
                result.response(),
                toolCallsJson,
                result.tokenUsage()
        );
    }
}
