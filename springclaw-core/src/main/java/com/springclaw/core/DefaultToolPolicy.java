package com.springclaw.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link ToolPolicy} with allowlist and denylist support.
 *
 * <p>Uses simple glob pattern matching (* and ?) for flexible tool filtering.
 * Denylist takes precedence over allowlist. Agent-specific rules override global rules.
 */
public class DefaultToolPolicy implements ToolPolicy {

    private final Map<String, Set<String>> globalAllowList = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> globalDenyList = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> agentAllowList = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> agentDenyList = new ConcurrentHashMap<>();

    @Override
    public boolean isToolAllowed(ToolDefinition tool, String agentId) {
        String toolName = tool.getName();

        // Check agent-specific denylist first (highest priority)
        Set<String> agentDeny = agentDenyList.get(agentId);
        if (agentDeny != null && isDenied(agentDeny, toolName)) {
            return false;
        }

        // Check agent-specific allowlist
        Set<String> agentAllow = agentAllowList.get(agentId);
        if (agentAllow != null) {
            return isAllowed(agentAllow, toolName);
        }

        // Check global denylist
        Set<String> globalDeny = globalDenyList.get("*");
        if (globalDeny != null && isDenied(globalDeny, toolName)) {
            return false;
        }

        // Check global allowlist (apply to all agents via "*" key)
        Set<String> globalAllow = globalAllowList.get("*");
        if (globalAllow != null && !globalAllow.isEmpty()) {
            return isAllowed(globalAllow, toolName);
        }

        // No allowlist = everything allowed (unless denied above)
        return true;
    }

    private boolean isDenied(Set<String> denyList, String toolName) {
        for (String pattern : denyList) {
            if (ToolPolicy.matchesGlob(toolName, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowed(Set<String> allowList, String toolName) {
        for (String pattern : allowList) {
            if (ToolPolicy.matchesGlob(toolName, pattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setAllowList(Map<String, Set<String>> allowList) {
        this.globalAllowList.clear();
        if (allowList != null) {
            this.globalAllowList.putAll(allowList);
        }
    }

    @Override
    public void setDenyList(Map<String, Set<String>> denyList) {
        this.globalDenyList.clear();
        if (denyList != null) {
            this.globalDenyList.putAll(denyList);
        }
    }

    /**
     * Set agent-specific allowlist.
     */
    public void setAgentAllowList(String agentId, Set<String> tools) {
        agentAllowList.computeIfAbsent(agentId, k -> ConcurrentHashMap.newKeySet()).clear();
        if (tools != null) {
            agentAllowList.get(agentId).addAll(tools);
        }
    }

    /**
     * Set agent-specific denylist.
     */
    public void setAgentDenyList(String agentId, Set<String> tools) {
        agentDenyList.computeIfAbsent(agentId, k -> ConcurrentHashMap.newKeySet()).clear();
        if (tools != null) {
            agentDenyList.get(agentId).addAll(tools);
        }
    }
}
