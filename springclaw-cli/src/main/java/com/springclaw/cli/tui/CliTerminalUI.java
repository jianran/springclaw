package com.springclaw.cli.tui;

import com.springclaw.cli.CliProperties;
import com.springclaw.cli.client.AgentClient;
import com.springclaw.cli.client.AgentClient.AgentInfo;
import com.springclaw.cli.client.AgentClient.ToolDefinition;
import com.springclaw.core.SessionContext;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interactive terminal UI for SpringClaw CLI.
 *
 * <p>Features:
 * <ul>
 *   <li>Multi-agent selection with interactive startup prompt</li>
 *   <li>Streaming responses with live token display</li>
 *   <li>Tool call visibility during execution</li>
 *   <li>Command system (/agents, /tools, /reset, /help, /quit)</li>
 *   <li>Input history with up/down arrow navigation</li>
 *   <li>Tab completion for commands and agent names</li>
 * </ul>
 *
 * <p>Uses JLine for rich terminal interaction.
 * Runs in a background thread, allowing the application to continue.
 */
public class CliTerminalUI {

    private static final Logger log = LoggerFactory.getLogger(CliTerminalUI.class);

    // ANSI color codes
    private static final String CYAN = "\033[36m";
    private static final String YELLOW = "\033[33m";
    private static final String GREEN = "\033[32m";
    private static final String WHITE = "\033[37m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";
    private static final String RESET = "\033[0m";
    private static final String CLEAR_LINE = "\033[2K\r";

    private static final String APP_NAME = "SpringClaw";
    private static final String VERSION = "0.1.0";
    private static final int HISTORY_SIZE = 100;

    private final CliProperties properties;
    private final AgentClient agentClient;

    private volatile boolean isRunning = false;
    private volatile Terminal terminal;
    private volatile LineReader lineReader;
    private volatile org.jline.terminal.Attributes originalAttrs;
    private volatile PrintWriter writer;
    private volatile String currentAgentId;
    private volatile String currentSessionId;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "cli-tui-reader");
        t.setDaemon(true);
        return t;
    });

    private final Deque<String> history = new ArrayDeque<>(HISTORY_SIZE);
    private final AtomicInteger historyIndex = new AtomicInteger(-1);
    private final AtomicBoolean streamingDisplayed = new AtomicBoolean(false);

    public CliTerminalUI(CliProperties properties, AgentClient agentClient) {
        this.properties = properties;
        this.agentClient = agentClient;
    }

    /**
     * Start the TUI. Initializes terminal, resolves agent, and starts read loop.
     */
    public void start() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(false)
                    .encoding(java.nio.charset.StandardCharsets.UTF_8)
                    .build();

            originalAttrs = terminal.enterRawMode();
            writer = terminal.writer();

            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(getCompleter())
                    .build();

            resolveCurrentAgent();
            printBanner();

            isRunning = true;
            executor.submit(this::readLoop);

            log.info("TUI started — agent: {}, streaming: {}",
                    currentAgentId, properties.getTui().isStreaming());
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize TUI: " + e.getMessage(), e);
        }
    }

    /**
     * Await the TUI to exit (Ctrl+D or /quit).
     */
    public void await() throws InterruptedException {
        while (isRunning) {
            Thread.sleep(100);
        }
    }

    /**
     * Stop the TUI gracefully.
     */
    public void stop() {
        isRunning = false;
        executor.shutdownNow();
        if (originalAttrs != null) {
            terminal.setAttributes(originalAttrs);
        }
        if (terminal != null) {
            try {
                terminal.close();
            } catch (IOException e) {
                log.warn("Error closing terminal: {}", e.getMessage());
            }
        }
        log.info("TUI stopped");
    }

    // ---------------------------------------------------------------------------
    // Agent resolution & banner
    // ---------------------------------------------------------------------------

    private void resolveCurrentAgent() {
        Collection<AgentInfo> allAgents = agentClient.listAgents();
        List<AgentInfo> agentList = new ArrayList<>(allAgents);

        if (agentList.isEmpty()) {
            log.warn("No agents registered — TUI will wait for agents");
            currentAgentId = null;
            return;
        }

        // Use configured agent or first available
        if (properties.getDefaultAgent() != null && !properties.getDefaultAgent().isBlank()) {
            currentAgentId = properties.getDefaultAgent();
            if (agentClient.getAgentInfo(currentAgentId) == null) {
                log.warn("Configured agent '{}' not found — showing selector", properties.getDefaultAgent());
                currentAgentId = null;
            }
        }

        if (currentAgentId == null && agentList.size() == 1) {
            currentAgentId = agentList.get(0).id();
            return;
        }

        // Multiple agents: prompt user
        if (currentAgentId == null) {
            System.out.println();
            System.out.println("  Available agents:");
            for (int i = 0; i < agentList.size(); i++) {
                AgentInfo agent = agentList.get(i);
                System.out.println(String.format("    %d) %s — %s", i + 1, agent.name(), agent.description()));
            }
            System.out.print("\n  Select agent [1-" + agentList.size() + "]: ");
            System.out.flush();
            try {
                String choice = lineReader.readLine().trim();
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < agentList.size()) {
                    currentAgentId = agentList.get(index).id();
                } else {
                    currentAgentId = agentList.get(0).id();
                }
            } catch (Exception e) {
                currentAgentId = agentList.get(0).id();
            }
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println(BOLD + "  ╔═══════════════════════════════╗" + RESET);
        System.out.println(BOLD + "  ║         " + APP_NAME + " CLI v" + VERSION + "              ║" + RESET);
        System.out.println(BOLD + "  ║        Interactive Mode       ║" + RESET);
        System.out.println(BOLD + "  ╚═══════════════════════════════╝" + RESET);
        System.out.println();

        AgentInfo agent = agentClient.getAgentInfo(currentAgentId);
        if (agent != null) {
            System.out.println("  Agent: " + BOLD + agent.name() + RESET + " (" + agent.id() + ")");
            if (agent.description() != null && !agent.description().isEmpty()) {
                System.out.println("  " + DIM + agent.description() + RESET);
            }
            if (agent.systemPrompt() != null && !agent.systemPrompt().isEmpty()) {
                System.out.println();
                System.out.println("  System prompt:");
                for (String line : agent.systemPrompt().split("\n")) {
                    System.out.println("    " + DIM + line.trim() + RESET);
                }
            }
        }

        System.out.println();
        System.out.println("  Commands: " + YELLOW + "/agents  /tools  /session  /reset  /help  /quit" + RESET);
        System.out.println("  " + DIM + "Up/Down for history, Tab for completion, Ctrl+D to quit" + RESET);
        System.out.println();
    }

    // ---------------------------------------------------------------------------
    // Read loop
    // ---------------------------------------------------------------------------

    private void readLoop() {
        while (isRunning) {
            try {
                String prompt = getPrompt();
                String line = lineReader.readLine(prompt);
                if (line == null) break; // EOF

                line = line.trim();
                if (line.isEmpty()) continue;

                // Handle history
                history.addLast(line);
                if (history.size() > HISTORY_SIZE) history.pollFirst();
                historyIndex.set(-1);

                // Handle commands
                if (line.startsWith("/")) {
                    handleCommand(line);
                    continue;
                }

                // Validate agent
                if (currentAgentId == null) {
                    System.out.println("  " + YELLOW + "No agent selected. Use /agents to choose one." + RESET);
                    continue;
                }

                String sessionId = resolveSessionId();

                if (properties.getTui().isStreaming()) {
                    startStreamingDisplay(sessionId, line);
                } else {
                    try {
                        var result = agentClient.prompt(currentAgentId, line, sessionId);
                        writeResponse(result);
                    } catch (Exception e) {
                        System.out.println("  " + YELLOW + "Error: " + e.getMessage() + RESET);
                    }
                }

            } catch (Exception e) {
                if (isRunning) log.error("TUI read error: {}", e.getMessage());
                break;
            }
        }

        System.out.println();
        System.out.println("  " + DIM + "Session ended. Goodbye!" + RESET);
        System.out.println();
    }

    /**
     * Start a streaming display for the current message.
     */
    private void startStreamingDisplay(String sessionId, String userMessage) {
        executor.submit(() -> {
            try {
                AgentInfo agent = agentClient.getAgentInfo(currentAgentId);
                if (agent == null) return;

                SessionContext ctx = new SessionContext(
                        sessionId, "cli",
                        Map.of("agentId", currentAgentId, "sessionId", sessionId));

                writer.print(CLEAR_LINE + "  " + DIM + "[streaming...] " + RESET);
                writer.flush();

                Flux<String> stream = agentClient.streamPrompt(currentAgentId, userMessage, sessionId);

                stream.doOnNext(chunk -> {
                    try {
                        writer.print(chunk);
                        writer.flush();
                    } catch (Exception e) {
                        log.debug("Stream write error: {}", e.getMessage());
                    }
                })
                .doOnComplete(() -> {
                    try {
                        writer.println();
                        writer.println();
                        writer.flush();
                    } catch (Exception e) {
                        log.debug("Stream flush error: {}", e.getMessage());
                    }
                })
                .doOnComplete(() -> streamingDisplayed.set(true))
                .blockLast();

            } catch (Exception e) {
                log.error("Streaming display error: {}", e.getMessage());
                streamingDisplayed.set(true);
            }
        });
    }

    private String getPrompt() {
        AgentInfo agent = agentClient.getAgentInfo(currentAgentId);
        String agentName = agent != null ? agent.name() : "?";
        return CYAN + "[" + agentName + "] > " + RESET;
    }

    // ---------------------------------------------------------------------------
    // Tab completion
    // ---------------------------------------------------------------------------

    private Completer getCompleter() {
        return (reader, line, candidates) -> {
            String buffer = line.line();
            if (!buffer.startsWith("/")) return;

            String[] parts = buffer.split("\\s+", 2);
            String cmdPart = parts[0];

            List<String> commands = List.of("/agents", "/tools", "/session", "/reset", "/help", "/quit", "/exit");

            List<String> matches = new ArrayList<>();
            for (String cmd : commands) {
                if (cmd.startsWith(cmdPart.substring(1))) {
                    matches.add(cmd);
                }
            }

            // For /agents, also complete agent IDs
            if (parts.length > 1) {
                String prefix = "/agents";
                String afterPrefix = cmdPart.length() > prefix.length()
                        ? cmdPart.substring(prefix.length()) : "";
                if (prefix.startsWith(afterPrefix) || afterPrefix.isEmpty()) {
                    for (AgentInfo agent : agentClient.listAgents()) {
                        String id = agent.id();
                        if (!matches.contains("/agents " + id) && id.startsWith(afterPrefix)) {
                            matches.add("/agents " + id);
                        }
                    }
                }
            }

            for (String match : matches) {
                candidates.add(new Candidate(match));
            }
        };
    }

    // ---------------------------------------------------------------------------
    // Commands
    // ---------------------------------------------------------------------------

    private void handleCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/agents" -> handleAgentsCommand(parts.length > 1 ? parts[1] : null);
            case "/tools" -> handleToolsCommand();
            case "/session" -> handleSessionCommand();
            case "/reset" -> handleResetCommand();
            case "/help" -> handleHelpCommand();
            case "/quit", "/exit" -> isRunning = false;
            default -> System.out.println("  " + YELLOW + "Unknown command: " + cmd + RESET
                    + DIM + " — type /help for available commands" + RESET);
        }
    }

    private void handleAgentsCommand(String agentId) {
        if (agentId != null) {
            AgentInfo target = agentClient.getAgentInfo(agentId);
            if (target != null) {
                currentAgentId = target.id();
                currentSessionId = null;
                System.out.println("  " + GREEN + "Switched to agent: " + target.name() + RESET);
                System.out.println("  " + DIM + "Session reset for new agent" + RESET);
            } else {
                System.out.println("  " + YELLOW + "Agent '" + agentId + "' not found." + RESET);
            }
            return;
        }

        Collection<AgentInfo> allAgents = agentClient.listAgents();
        System.out.println();
        System.out.println("  " + BOLD + "Available agents:" + RESET);
        for (AgentInfo agent : allAgents) {
            String marker = agent.id().equals(currentAgentId) ? GREEN + " > " + RESET : "    ";
            System.out.println(String.format("    %s%s (%s)", marker, BOLD + agent.name() + RESET, agent.id()));
            if (agent.description() != null && !agent.description().isEmpty()) {
                System.out.println("       " + DIM + agent.description() + RESET);
            }
        }
        System.out.println();
        System.out.println("  " + DIM + "Switch: /agents <agent-id>" + RESET);
    }

    private void handleToolsCommand() {
        AgentInfo agent = agentClient.getAgentInfo(currentAgentId);
        if (agent == null) {
            System.out.println("  " + YELLOW + "No agent selected" + RESET);
            return;
        }

        Collection<ToolDefinition> tools = agentClient.listTools(currentAgentId);
        if (tools.isEmpty()) {
            System.out.println("  " + DIM + "No tools registered for this agent" + RESET);
            return;
        }

        System.out.println();
        System.out.println("  " + BOLD + "Tools for " + agent.name() + ":" + RESET);
        for (ToolDefinition tool : tools) {
            System.out.println("    " + YELLOW + tool.name() + RESET);
            if (tool.description() != null && !tool.description().isEmpty()) {
                System.out.println("       " + DIM + tool.description() + RESET);
            }
        }
        System.out.println();
    }

    private void handleSessionCommand() {
        System.out.println();
        System.out.println("  " + BOLD + "Session info:" + RESET);
        System.out.println("    Session ID: " + DIM + currentSessionId + RESET);
        System.out.println("    Use /reset to clear");
        System.out.println();
    }

    private void handleResetCommand() {
        if (currentSessionId != null) {
            agentClient.resetSession(currentAgentId, currentSessionId);
        }
        currentSessionId = null;
        System.out.println("  " + GREEN + "Session reset" + RESET);
    }

    private void handleHelpCommand() {
        System.out.println();
        System.out.println("  " + BOLD + "Commands:" + RESET);
        System.out.println("    /agents [id]    List agents or switch to agent");
        System.out.println("    /tools          List tools for current agent");
        System.out.println("    /session        Show session information");
        System.out.println("    /reset          Reset current session");
        System.out.println("    /help           Show this help message");
        System.out.println("    /quit, /exit    Exit the TUI");
        System.out.println();
        System.out.println("  " + BOLD + "Keyboard shortcuts:" + RESET);
        System.out.println("    Up/Down         Navigate command history");
        System.out.println("    Tab             Complete commands / agent names");
        System.out.println("    Ctrl+D          Exit");
        System.out.println();
    }

    // ---------------------------------------------------------------------------
    // Response display
    // ---------------------------------------------------------------------------

    private void writeResponse(AgentClient.PromptResult result) {
        if (result == null || result.response() == null || result.response().isEmpty()) {
            return;
        }

        try {
            writer.println();
            writer.println();

            // Show tool calls if present
            if (properties.getTui().isShowToolCalls() && !result.toolCalls().equals("[]")) {
                writer.print("  " + YELLOW + "→ tools: " + RESET + result.toolCalls());
            }

            // Display response
            writer.print("  " + WHITE + result.response() + RESET);

            // Show token usage
            if (properties.getTui().isShowTokenUsage() && result.tokenUsage().totalTokens() > 0) {
                writer.print("  " + DIM + "\n  [tokens: " + result.tokenUsage().promptTokens()
                        + "+" + result.tokenUsage().completionTokens()
                        + "=" + result.tokenUsage().totalTokens() + "]" + RESET);
            }

            writer.println();
            writer.println();
            writer.flush();
        } catch (Exception e) {
            log.error("Error writing response: {}", e.getMessage());
        }
    }

    private String resolveSessionId() {
        if (currentSessionId == null) {
            currentSessionId = UUID.randomUUID().toString();
        }
        return currentSessionId;
    }
}
