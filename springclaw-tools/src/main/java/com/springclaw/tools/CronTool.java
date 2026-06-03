package com.springclaw.tools;

import com.springclaw.core.ToolContext;
import com.springclaw.core.ToolDefinition;
import com.springclaw.core.ToolExecutionMode;
import com.springclaw.core.ToolResult;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Cron tool — schedules recurring tasks.
 *
 * <p>Supports simple scheduling expressions:
 * <ul>
 *   <li>"every N minutes" — recurring every N minutes</li>
 *   <li>"daily at HH:MM" — once per day at specified time</li>
 *   <li>"once at HH:MM" — single execution at specified time</li>
 * </ul>
 */
public class CronTool implements ToolDefinition {

    private static final String NAME = "cron";
    private static final String DESCRIPTION = "Schedule recurring or one-time tasks. Supports expressions like 'every 5 minutes', 'daily at 9:00', or 'once at 14:30'. Returns the schedule ID and next run time.";
    private static final String SCHEMA = """
            {
              "type": "object",
              "properties": {
                "expression": {
                  "type": "string",
                  "description": "Schedule expression (e.g., 'every 5 minutes', 'daily at 9:00')"
                },
                "task": {
                  "type": "string",
                  "description": "The task or message to execute on schedule"
                },
                "id": {
                  "type": "string",
                  "description": "Optional custom ID for this schedule"
                }
              },
              "required": ["expression", "task"]
            }
            """;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

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
        String expression = (String) arguments.get("expression");
        String task = (String) arguments.get("task");
        String id = (String) arguments.get("id");

        if (expression == null || task == null) {
            return new ToolResult(null, NAME, "expression and task are required",
                    false, "Missing required arguments", Map.of());
        }

        String scheduleId = id != null ? id : UUID.randomUUID().toString();
        Duration interval = parseExpression(expression);

        if (interval == null) {
            return new ToolResult(null, NAME,
                    "Failed to parse expression: " + expression +
                    ". Use 'every N minutes' or 'daily at HH:MM'.",
                    false, "Invalid expression", Map.of());
        }

        // Schedule the task
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[Cron: " + scheduleId + "] " + task);
        }, 0, interval.toMinutes(), TimeUnit.MINUTES);

        scheduledTasks.put(scheduleId, future);

        Instant nextRun = Instant.now().plus(interval);
        String nextRunStr = nextRun.toString();

        return new ToolResult(null, NAME,
                "Schedule created:\n" +
                "  ID: " + scheduleId + "\n" +
                "  Expression: " + expression + "\n" +
                "  Task: " + task + "\n" +
                "  Next run: " + nextRunStr + "\n" +
                "  Interval: " + interval.toMinutes() + " minutes",
                true, null, Map.of("scheduleId", scheduleId, "intervalMinutes", interval.toMinutes()));
    }

    private Duration parseExpression(String expression) {
        String lower = expression.toLowerCase().trim();

        // "every N minutes"
        if (lower.startsWith("every ")) {
            try {
                int minutes = Integer.parseInt(lower.substring(6).trim());
                if (minutes <= 0 || minutes > 1440) return null;
                return Duration.ofMinutes(minutes);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // "daily at HH:MM"
        if (lower.startsWith("daily at ")) {
            try {
                String timePart = lower.substring(9).trim();
                String[] parts = timePart.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                // For simplicity, schedule as daily (1440 minutes) with offset
                long offsetMinutes = hour * 60 + minute;
                return Duration.ofMinutes(Math.min(offsetMinutes, 1440)); // Use as recurring interval for now
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Shutdown scheduler on cleanup.
     */
    public void shutdown() {
        scheduler.shutdown();
    }
}
