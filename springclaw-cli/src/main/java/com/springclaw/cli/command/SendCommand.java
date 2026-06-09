package com.springclaw.cli.command;

import com.springclaw.cli.CliProperties;
import com.springclaw.cli.client.AgentClient;
import com.springclaw.cli.client.AgentClient.PromptResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Send command — one-shot prompt with optional formatting.
 *
 * <p>Usage: send <message> [-a <agentId>] [-f <json|text>] [-s <sessionId>]
 */
public class SendCommand {

    private static final Logger log = LoggerFactory.getLogger(SendCommand.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public void execute(AgentClient agentClient, CliProperties properties, String[] args) {
        String message = null;
        String agentId = null;
        String sessionId = null;
        String format = "text";

        // Parse arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-a", "--agent" -> agentId = args[++i];
                case "-f", "--format" -> format = args[++i];
                case "-s", "--session" -> sessionId = args[++i];
                default -> {
                    if (message == null) message = args[i];
                }
            }
        }

        if (message == null) {
            System.err.println("Usage: send <message> [-a <agentId>] [-f <json|text>]");
            System.exit(1);
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

        // Resolve session
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }

        // Execute prompt
        PromptResult result;
        try {
            result = agentClient.prompt(agentId, message, sessionId);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
            return;
        }

        // Output result
        if ("json".equals(format)) {
            try {
                System.out.println(mapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                System.err.println("Failed to serialize result: " + e.getMessage());
                System.exit(1);
            }
        } else {
            // Text format
            System.out.println("[" + result.agentId() + "] " + result.response());
            if (result.tokenUsage().totalTokens() > 0) {
                System.out.println("[tokens: " + result.tokenUsage().promptTokens()
                        + "+" + result.tokenUsage().completionTokens()
                        + "=" + result.tokenUsage().totalTokens() + "]");
            }
            if (!result.toolCalls().equals("[]")) {
                System.out.println("[tools: " + result.toolCalls() + "]");
            }
        }
    }
}
