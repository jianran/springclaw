package com.springclaw.cli.command;

import com.springclaw.cli.CliProperties;
import com.springclaw.cli.client.AgentClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Session command — show or reset session info.
 *
 * <p>Usage: session [-f <json|text>] [--reset] [<agentId>]
 */
public class SessionCommand {

    private static final ObjectMapper mapper = new ObjectMapper();

    public void execute(AgentClient agentClient, CliProperties properties, String[] args) {
        String format = "text";
        boolean reset = false;
        String agentId = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-f", "--format" -> format = args[++i];
                case "--reset" -> reset = true;
                default -> agentId = args[i];
            }
        }

        // Resolve agent ID
        if (agentId == null) {
            agentId = properties.getDefaultAgent();
        }

        if (agentId == null) {
            var agents = agentClient.listAgents();
            if (agents.isEmpty()) {
                System.err.println("No agents available");
                System.exit(1);
            }
            agentId = agents.iterator().next().id();
        }

        if (reset) {
            agentClient.resetSession(agentId, null);
            System.out.println("Session reset for agent: " + agentId);
            return;
        }

        if ("json".equals(format)) {
            try {
                System.out.println(mapper.writeValueAsString(
                        java.util.Map.of("agentId", agentId, "sessionId", "active")));
            } catch (JsonProcessingException e) {
                System.err.println("Failed to serialize session info: " + e.getMessage());
            }
            return;
        }

        // Text format
        System.out.println();
        System.out.println("  Session info for " + bold(agentId) + ":");
        System.out.println("    Session: active");
        System.out.println("    Use --reset to clear the session");
        System.out.println();
    }

    private String bold(String text) {
        return "\033[1m" + text + "\033[0m";
    }
}
