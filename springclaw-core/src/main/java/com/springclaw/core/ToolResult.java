package com.springclaw.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of executing a Tool.
 *
 * @param toolCallId the ID of the tool call this result belongs to
 * @param toolName   name of the tool that was executed
 * @param content    the tool's output (success or error message)
 * @param success    whether the execution succeeded
 * @param error      error message if execution failed
 * @param metadata   additional result metadata
 */
public record ToolResult(
    String toolCallId,
    String toolName,
    String content,
    boolean success,
    String error,
    Map<String, Object> metadata
) {
    public ToolResult {
        if (content == null) content = "";
        if (metadata == null) metadata = new HashMap<>();
    }
}
