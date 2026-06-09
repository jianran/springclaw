package com.springclaw.cli.command;

import com.springclaw.cli.CliProperties;
import com.springclaw.cli.client.AgentClient;

/**
 * Chat command — starts the interactive TUI session.
 */
public class ChatCommand {

    /**
     * Execute the chat command (no-op, handled by dispatcher).
     */
    public void execute(AgentClient agentClient, CliProperties properties, String[] args) {
        // The dispatcher handles starting the TUI directly.
        // This class exists as a hook for future argument parsing.
    }
}
