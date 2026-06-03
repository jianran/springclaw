package com.springclaw.tools;

import com.springclaw.core.ToolContext;
import com.springclaw.core.ToolDefinition;
import com.springclaw.core.ToolExecutionMode;
import com.springclaw.core.ToolResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session management tool.
 *
 * <p>Provides tools for listing active sessions, getting conversation
 * history, compacting sessions, and resetting sessions.
 */
public class SessionTool implements ToolDefinition {

    private static final String NAME = "session";
    private static final String DESCRIPTION = "Manage conversation sessions. Supports 'list' (list sessions), 'history' (get conversation history), 'compact' (remove old messages), and 'reset' (clear a session).";
    private static final String SCHEMA = """
            {
              "type": "object",
              "properties": {
                "operation": {
                  "type": "string",
                  "enum": ["list", "history", "compact", "reset"],
                  "description": "The session operation to perform"
                },
                "sessionId": {
                  "type": "string",
                  "description": "Session ID (required for history, compact, reset)"
                },
                "limit": {
                  "type": "integer",
                  "description": "Number of messages to return (for history, default 20)"
                }
              },
              "required": ["operation"]
            }
            """;

    // In-memory session store — in production, this would be backed by a database
    private final Map<String, List<String>> sessionHistory = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getSchema() {
        return SCHEMA;
    }

    @Override
    public ToolExecutionMode getExecutionMode() {
        return ToolExecutionMode.SEQUENTIAL;
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments, ToolContext context) {
        String operation = (String) arguments.get("operation");
        String sessionId = (String) arguments.get("sessionId");
        int limit = arguments.containsKey("limit")
                ? ((Number) arguments.get("limit")).intValue()
                : 20;

        if (operation == null) {
            return new ToolResult(null, NAME, "operation is required",
                    false, "Missing operation", Map.of());
        }

        return switch (operation.toLowerCase()) {
            case "list" -> listSessions(context);
            case "history" -> getHistory(sessionId, limit, context);
            case "compact" -> compactSession(sessionId, limit, context);
            case "reset" -> resetSession(sessionId, context);
            default -> new ToolResult(null, NAME,
                    "Unknown operation: " + operation + ". Use 'list', 'history', 'compact', or 'reset'.",
                    false, "Invalid operation", Map.of());
        };
    }

    private ToolResult listSessions(ToolContext context) {
        List<String> sessions = new ArrayList<>(sessionHistory.keySet());
        if (sessions.isEmpty()) {
            return new ToolResult(null, NAME,
                    "No active sessions found.", true, null, Map.of("count", 0));
        }
        String result = "Active sessions (" + sessions.size() + "):\n";
        for (String sid : sessions) {
            int msgCount = sessionHistory.getOrDefault(sid, List.of()).size();
            result += String.format("  - %s (%d messages)%n", sid, msgCount);
        }
        return new ToolResult(null, NAME, result, true, null,
                Map.of("count", sessions.size()));
    }

    private ToolResult getHistory(String sessionId, int limit, ToolContext context) {
        if (sessionId == null) {
            return new ToolResult(null, NAME, "sessionId is required for 'history' operation",
                    false, "Missing sessionId", Map.of());
        }

        List<String> messages = sessionHistory.getOrDefault(sessionId, List.of());
        int from = Math.max(0, messages.size() - limit);
        List<String> recent = messages.subList(from, messages.size());

        if (recent.isEmpty()) {
            return new ToolResult(null, NAME, "No messages found for session: " + sessionId,
                    true, null, Map.of("messageCount", 0));
        }

        String result = "Session: " + sessionId + " (" + recent.size() + " messages):\n";
        for (int i = 0; i < recent.size(); i++) {
            result += String.format("  %d. %s%n", i + 1, recent.get(i));
        }
        return new ToolResult(null, NAME, result, true, null,
                Map.of("messageCount", recent.size()));
    }

    private ToolResult compactSession(String sessionId, int limit, ToolContext context) {
        if (sessionId == null) {
            return new ToolResult(null, NAME, "sessionId is required for 'compact' operation",
                    false, "Missing sessionId", Map.of());
        }

        List<String> messages = sessionHistory.get(sessionId);
        if (messages == null || messages.isEmpty()) {
            return new ToolResult(null, NAME, "No messages to compact for session: " + sessionId,
                    true, null, Map.of());
        }

        int kept = Math.min(limit, messages.size());
        sessionHistory.put(sessionId, messages.subList(messages.size() - kept, messages.size()));

        return new ToolResult(null, NAME,
                "Compacted session '" + sessionId + "': kept last " + kept + " messages",
                true, null, Map.of("keptMessages", kept));
    }

    private ToolResult resetSession(String sessionId, ToolContext context) {
        if (sessionId == null) {
            return new ToolResult(null, NAME, "sessionId is required for 'reset' operation",
                    false, "Missing sessionId", Map.of());
        }

        sessionHistory.remove(sessionId);
        return new ToolResult(null, NAME,
                "Session '" + sessionId + "' has been reset.",
                true, null, Map.of());
    }
}
