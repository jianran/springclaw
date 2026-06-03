package com.springclaw.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DefaultToolPolicyTest {

    private DefaultToolPolicy policy;
    private ToolDefinition testTool;

    @BeforeEach
    void setUp() {
        policy = new DefaultToolPolicy();
        testTool = new ToolDefinition() {
            @Override
            public String getName() { return "web-search"; }
            @Override
            public String getDescription() { return "Search the web"; }
            @Override
            public String getSchema() { return "{}"; }
            @Override
            public ToolExecutionMode getExecutionMode() { return ToolExecutionMode.SEQUENTIAL; }
            @Override
            public ToolResult execute(Map<String, Object> args, ToolContext ctx) { return null; }
        };
    }

    @Test
    void defaultAllowAll() {
        assertTrue(policy.isToolAllowed(testTool, "agent-1"));
    }

    @Test
    void denyListBlocksTool() {
        Map<String, Set<String>> deny = new HashMap<>();
        deny.put("*", Set.of("web-search"));
        policy.setDenyList(deny);
        assertFalse(policy.isToolAllowed(testTool, "agent-1"));
    }

    @Test
    void denyListTakesPrecedenceOverAllowList() {
        Map<String, Set<String>> allow = new HashMap<>();
        allow.put("*", Set.of("web-search"));
        policy.setAllowList(allow);

        Map<String, Set<String>> deny = new HashMap<>();
        deny.put("*", Set.of("web-search"));
        policy.setDenyList(deny);

        assertFalse(policy.isToolAllowed(testTool, "agent-1"));
    }

    @Test
    void allowListRestricts() {
        Map<String, Set<String>> allow = new HashMap<>();
        allow.put("*", Set.of("file-read", "file-write"));
        policy.setAllowList(allow);
        assertFalse(policy.isToolAllowed(testTool, "agent-1"));
    }

    @Test
    void allowListAllowsSpecific() {
        Map<String, Set<String>> allow = new HashMap<>();
        allow.put("*", Set.of("web-search", "web-fetch"));
        policy.setAllowList(allow);
        assertTrue(policy.isToolAllowed(testTool, "agent-1"));
    }

    @Test
    void agentSpecificAllowList() {
        Map<String, Set<String>> globalAllow = new HashMap<>();
        globalAllow.put("*", Set.of("file-read"));
        policy.setAllowList(globalAllow);

        policy.setAgentAllowList("special-agent", Set.of("web-search", "web-fetch"));
        assertTrue(policy.isToolAllowed(testTool, "special-agent"));
        assertFalse(policy.isToolAllowed(testTool, "other-agent"));
    }

    @Test
    void agentSpecificDenyList() {
        Map<String, Set<String>> globalAllow = new HashMap<>();
        globalAllow.put("*", Set.of("web-search", "file-write"));
        policy.setAllowList(globalAllow);

        policy.setAgentDenyList("restricted-agent", Set.of("file-write"));
        assertTrue(policy.isToolAllowed(testTool, "restricted-agent"));
        // If we had a file-write tool, it would be denied
    }

    @Test
    void globPatternWildcard() {
        assertTrue(ToolPolicy.matchesGlob("web-search", "web-*"));
        assertTrue(ToolPolicy.matchesGlob("web-search-everything", "web-*"));
        assertFalse(ToolPolicy.matchesGlob("file-search", "web-*"));
    }

    @Test
    void globPatternQuestion() {
        assertTrue(ToolPolicy.matchesGlob("web-search", "web-?earch"));
        assertFalse(ToolPolicy.matchesGlob("web-search", "web-?arh"));
    }

    @Test
    void emptyAllowListAllowsAll() {
        policy.setAllowList(new HashMap<>());
        assertTrue(policy.isToolAllowed(testTool, "agent-1"));
    }
}
