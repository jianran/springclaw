package com.springclaw.cli;

import com.springclaw.cli.client.AgentClient;
import com.springclaw.cli.command.AgentsCommand;
import com.springclaw.cli.command.ChatCommand;
import com.springclaw.cli.command.SendCommand;
import com.springclaw.cli.command.SessionCommand;
import com.springclaw.cli.command.ToolsCommand;
import com.springclaw.cli.tui.CliTerminalUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * CLI command dispatcher.
 *
 * <p>Parses command-line arguments and routes to the appropriate
 * command handler. Runs as a {@link CommandLineRunner} bean.
 *
 * <p>Supported subcommands:
 * <ul>
 *   <li>{@code chat} — interactive TUI mode</li>
 *   <li>{@code send <message>} — one-shot prompt</li>
 *   <li>{@code agents} — list agents</li>
 *   <li>{@code tools} — list tools for current agent</li>
 *   <li>{@code session} — show/reset session info</li>
 * </ul>
 */
@Component
public class CliDispatcher implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CliDispatcher.class);

    private static final String[] SUBCOMMANDS = {"chat", "send", "agents", "tools", "session"};

    private final CliProperties properties;
    private final AgentClient agentClient;
    private final ChatCommand chatCommand;
    private final SendCommand sendCommand;
    private final AgentsCommand agentsCommand;
    private final ToolsCommand toolsCommand;
    private final SessionCommand sessionCommand;

    public CliDispatcher(CliProperties properties,
                         AgentClient agentClient,
                         ChatCommand chatCommand,
                         SendCommand sendCommand,
                         AgentsCommand agentsCommand,
                         ToolsCommand toolsCommand,
                         SessionCommand sessionCommand) {
        this.properties = properties;
        this.agentClient = agentClient;
        this.chatCommand = chatCommand;
        this.sendCommand = sendCommand;
        this.agentsCommand = agentsCommand;
        this.toolsCommand = toolsCommand;
        this.sessionCommand = sessionCommand;
    }

    @Override
    public void run(String... args) throws Exception {
        String subcommand = resolveSubcommand(args);

        if (subcommand == null) {
            // No subcommand: start interactive TUI by default
            if (properties.getTui().isEnabled()) {
                log.info("No subcommand specified — starting interactive TUI");
                startTUI();
            } else {
                printUsage();
            }
            return;
        }

        // Route to appropriate handler
        String[] remainingArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        switch (subcommand) {
            case "chat" -> startTUI();
            case "send" -> sendCommand.execute(agentClient, properties, remainingArgs);
            case "agents" -> agentsCommand.execute(agentClient, properties, remainingArgs);
            case "tools" -> toolsCommand.execute(agentClient, properties, remainingArgs);
            case "session" -> sessionCommand.execute(agentClient, properties, remainingArgs);
            default -> {
                log.warn("Unknown subcommand: {}", subcommand);
                printUsage();
            }
        }
    }

    /**
     * Resolve the subcommand from args.
     */
    private String resolveSubcommand(String[] args) {
        for (String arg : args) {
            for (String sub : SUBCOMMANDS) {
                if (arg.equals(sub)) return sub;
            }
        }
        return null;
    }

    /**
     * Start the interactive TUI.
     */
    private void startTUI() {
        CliTerminalUI tui = new CliTerminalUI(properties, agentClient);
        tui.start();
        try {
            tui.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            tui.stop();
        }
    }

    /**
     * Print usage information.
     */
    private void printUsage() {
        System.out.println();
        System.out.println("  SpringClaw CLI — AI Agent Framework");
        System.out.println();
        System.out.println("  Usage: springclaw-cli [options] <command> [args]");
        System.out.println();
        System.out.println("  Commands:");
        System.out.println("    chat               Start interactive TUI session");
        System.out.println("    send <message>     Send a one-shot prompt");
        System.out.println("    agents             List available agents");
        System.out.println("    tools              List tools for current agent");
        System.out.println("    session            Show or reset session info");
        System.out.println();
        System.out.println("  Options:");
        System.out.println("    -a, --agent <id>   Specify agent ID");
        System.out.println("    -f, --format <json|text>  Output format (default: text)");
        System.out.println("    --remote           Use remote mode (connect to gateway)");
        System.out.println("    --gateway-url <url> Gateway URL for remote mode");
        System.out.println("    -h, --help         Show this help message");
        System.out.println();
    }
}
