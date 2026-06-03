package com.springclaw.spring.boot;

import com.springclaw.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that manages tools across all agents with policy enforcement.
 */
public class SpringClawToolRegistry implements ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(SpringClawToolRegistry.class);

    private final AgentRegistry agentRegistry;
    private ToolPolicy policy;
    private final Map<String, List<ToolDefinition>> agentTools = new ConcurrentHashMap<>();
    private final List<FunctionCallback> callbacks = new ArrayList<>();
    private final List<ToolCallbackProvider> callbackProviders;

    public SpringClawToolRegistry(AgentRegistry agentRegistry) {
        this(agentRegistry, new DefaultToolPolicy(), List.of());
    }

    public SpringClawToolRegistry(AgentRegistry agentRegistry, ToolPolicy policy, List<ToolCallbackProvider> callbackProviders) {
        this.agentRegistry = agentRegistry;
        this.policy = policy;
        this.callbackProviders = callbackProviders;
        // Scan callback providers for tools
        scanToolProviders();
    }

    private void scanToolProviders() {
        for (ToolCallbackProvider provider : callbackProviders) {
            FunctionCallback[] cbs = provider.getToolCallbacks();
            for (FunctionCallback cb : cbs) {
                this.callbacks.add(cb);
                log.debug("Discovered tool: {} ({})", cb.getName(), cb.getDescription());
            }
        }
    }

    @Override
    public List<ToolDefinition> getEffectiveTools(String agentId) {
        com.springclaw.core.Agent agent = agentRegistry.getAgent(agentId);
        if (agent == null) {
            return Collections.emptyList();
        }

        List<String> allowList = agent.getAllowedTools();
        List<String> denyList = agent.getDeniedTools();

        // If agent has explicit allow/deny from config, use those
        if (!allowList.isEmpty() || !denyList.isEmpty()) {
            DefaultToolPolicy agentPolicy = new DefaultToolPolicy();
            Map<String, Set<String>> allow = new HashMap<>();
            allow.put("*", new HashSet<>(allowList));
            agentPolicy.setAllowList(allow);

            Map<String, Set<String>> deny = new HashMap<>();
            deny.put("*", new HashSet<>(denyList));
            agentPolicy.setDenyList(deny);

            return filterTools(agent, agentPolicy);
        }

        return filterTools(agent, this.policy);
    }

    private List<ToolDefinition> filterTools(com.springclaw.core.Agent agent, ToolPolicy agentPolicy) {
        List<ToolDefinition> allTools = collectAllTools();
        return allTools.stream()
                .filter(tool -> tool.isAllowed(agent.getId(), agentPolicy))
                .toList();
    }

    private List<ToolDefinition> collectAllTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        for (FunctionCallback cb : callbacks) {
            tools.add(new SpringClawToolAdapter(cb));
        }
        return tools;
    }

    @Override
    public void registerTool(String agentId, ToolDefinition tool) {
        agentTools.computeIfAbsent(agentId, k -> new ArrayList<>()).add(tool);
        log.info("Registered tool '{}' for agent '{}'", tool.getName(), agentId);
    }

    @Override
    public void registerTool(ToolDefinition tool) {
        // Register globally for all agents
        agentRegistry.getAllAgents().forEach(agent ->
                registerTool(agent.getId(), tool)
        );
        if (agentRegistry.getAllAgents().isEmpty()) {
            // No agents registered yet; store for future agents
            agentTools.computeIfAbsent("__global__", k -> new ArrayList<>()).add(tool);
        }
    }

    @Override
    public void setPolicy(ToolPolicy policy) {
        this.policy = policy;
    }

    /**
     * Get tools registered for a specific agent (including global).
     */
    List<ToolDefinition> getAgentSpecificTools(String agentId) {
        List<ToolDefinition> tools = new ArrayList<>();
        agentTools.forEach((key, value) -> {
            if ("__global__".equals(key) || key.equals(agentId)) {
                tools.addAll(value);
            }
        });
        return tools;
    }
}
