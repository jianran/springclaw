package com.springclaw.cli.command;

import com.springclaw.cli.CliProperties;
import com.springclaw.cli.client.AgentClient;
import com.springclaw.cli.client.AgentClient.ToolDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;

/**
 * Tools command — list tools for an agent.
 *
 * <p>Usage: tools [-f <json|text>] [<agentId>]
 */
public class ToolsCommand {

    private static final ObjectMapper mapper = new ObjectMapper();

    public void execute(AgentClient agentClient, CliProperties properties, String[] args) {
        String format = "text";
        String agentId = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-f", "--format" -> format = args[++i];
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

        Collection<ToolDefinition> tools = agentClient.listTools(agentId);

        if (tools.isEmpty()) {
            System.out.println("No tools registered for agent: " + agentId);
            return;
        }

        if ("json".equals(format)) {
            try {
                System.out.println(mapper.writeValueAsString(tools));
            } catch (JsonProcessingException e) {
                System.err.println("Failed to serialize: " + e.getMessage());
                System.exit(1);
            }
            return;
        }

        // Text format
        System.out.println();
        System.out.println("  Tools for " + bold(agentId) + ":");
        for (ToolDefinition tool : tools) {
            System.out.println("    " + yellow(tool.name()));
            if (tool.description() != null && !tool.description().isEmpty()) {
                System.out.println("       " + dim(tool.description()));
            }
        }
        System.out.println();
    }

    private String bold(String text) {
        return "\033[1m" + text + "\033[0m";
    }

    private String yellow(String text) {
        return "\033[33m" + text + "\033[0m";
    }

    private String dim(String text) {
        return "\033[2m" + text + "\033[0m";
    }
}
