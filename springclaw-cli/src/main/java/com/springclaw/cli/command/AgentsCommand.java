package com.springclaw.cli.command;

import com.springclaw.cli.CliProperties;
import com.springclaw.cli.client.AgentClient;
import com.springclaw.cli.client.AgentClient.AgentInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.List;

/**
 * Agents command — list available agents or select a specific one.
 *
 * <p>Usage: agents [-f <json|text>] [<agentId>]
 */
public class AgentsCommand {

    private static final ObjectMapper mapper = new ObjectMapper();

    public void execute(AgentClient agentClient, CliProperties properties, String[] args) {
        String format = "text";
        String filterAgentId = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-f", "--format" -> format = args[++i];
                default -> filterAgentId = args[i];
            }
        }

        Collection<AgentInfo> agents;
        if (filterAgentId != null) {
            AgentInfo agent = agentClient.getAgentInfo(filterAgentId);
            if (agent == null) {
                System.err.println("Agent not found: " + filterAgentId);
                System.exit(1);
            }
            agents = List.of(agent);
        } else {
            agents = agentClient.listAgents();
        }

        if (agents.isEmpty()) {
            System.out.println("No agents available");
            return;
        }

        if ("json".equals(format)) {
            try {
                System.out.println(mapper.writeValueAsString(agents));
            } catch (JsonProcessingException e) {
                System.err.println("Failed to serialize: " + e.getMessage());
                System.exit(1);
            }
            return;
        }

        // Text format
        System.out.println();
        System.out.println("  Available agents:");
        for (AgentInfo agent : agents) {
            System.out.println("    " + bold(agent.name()) + " (" + agent.id() + ")");
            if (agent.description() != null && !agent.description().isEmpty()) {
                System.out.println("       " + dim(agent.description()));
            }
        }
        System.out.println();
    }

    private String bold(String text) {
        return "\033[1m" + text + "\033[0m";
    }

    private String dim(String text) {
        return "\033[2m" + text + "\033[0m";
    }
}
