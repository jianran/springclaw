package com.springclaw.tools;

import com.springclaw.core.ToolContext;
import com.springclaw.core.ToolDefinition;
import com.springclaw.core.ToolExecutionMode;
import com.springclaw.core.ToolResult;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

/**
 * File read tool — reads files from a workspace directory.
 *
 * <p>Supports reading individual files and listing directories.
 * Only reads within the configured workspace directory for safety.
 */
public class FileReadTool implements ToolDefinition {

    private static final String NAME = "file_read";
    private static final String DESCRIPTION = "Read files from the workspace directory. Supports reading file content and listing directory contents. Only reads within the configured workspace for safety.";
    private static final String SCHEMA = """
            {
              "type": "object",
              "properties": {
                "path": {
                  "type": "string",
                  "description": "The file or directory path relative to workspace"
                },
                "operation": {
                  "type": "string",
                  "enum": ["read", "list"],
                  "description": "Operation: 'read' for file content, 'list' for directory listing"
                },
                "maxLines": {
                  "type": "integer",
                  "description": "Maximum lines to return (default 100)"
                }
              },
              "required": ["path", "operation"]
            }
            """;

    private final Path workspaceDir;

    public FileReadTool() {
        this(System.getProperty("springclaw.workspace", "."));
    }

    public FileReadTool(String workspaceDir) {
        this.workspaceDir = Paths.get(workspaceDir).toAbsolutePath().normalize();
    }

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
        String path = (String) arguments.get("path");
        String operation = (String) arguments.get("operation");
        int maxLines = arguments.containsKey("maxLines")
                ? ((Number) arguments.get("maxLines")).intValue()
                : 100;

        if (path == null || operation == null) {
            return new ToolResult(null, NAME, "path and operation are required",
                    false, "Missing required arguments", Map.of());
        }

        Path target = workspaceDir.resolve(path).normalize();

        // Security: prevent directory traversal outside workspace
        if (!target.startsWith(workspaceDir)) {
            return new ToolResult(null, NAME,
                    "Access denied: path is outside workspace directory",
                    false, "Path traversal blocked", Map.of());
        }

        try {
            return switch (operation.toLowerCase()) {
                case "read" -> readFile(target, maxLines);
                case "list" -> listDirectory(target);
                default -> new ToolResult(null, NAME,
                        "Unknown operation: " + operation + ". Use 'read' or 'list'.",
                        false, "Invalid operation", Map.of());
            };
        } catch (IOException e) {
            return new ToolResult(null, NAME,
                    "Failed to " + operation + " '" + path + "': " + e.getMessage(),
                    false, e.getMessage(), Map.of());
        }
    }

    private ToolResult readFile(Path target, int maxLines) throws IOException {
        if (!Files.exists(target)) {
            return new ToolResult(null, NAME, "File not found: " + target,
                    false, "File not found", Map.of());
        }

        if (!Files.isRegularFile(target)) {
            return new ToolResult(null, NAME, "Not a file: " + target,
                    false, "Not a file", Map.of());
        }

        List<String> lines = Files.readAllLines(target);
        String content;
        if (lines.size() > maxLines) {
            content = String.join("\n", lines.subList(0, maxLines))
                    + "\n\n... (truncated, " + (lines.size() - maxLines) + " more lines)";
        } else {
            content = String.join("\n", lines);
        }

        return new ToolResult(null, NAME,
                "File: " + target.toAbsolutePath() + "\n" +
                "Size: " + lines.size() + " lines\n" +
                "Content:\n" + content,
                true, null, Map.of("lines", lines.size()));
    }

    private ToolResult listDirectory(Path target) throws IOException {
        if (!Files.exists(target)) {
            return new ToolResult(null, NAME, "Directory not found: " + target,
                    false, "Directory not found", Map.of());
        }

        if (!Files.isDirectory(target)) {
            return new ToolResult(null, NAME, "Not a directory: " + target,
                    false, "Not a directory", Map.of());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Directory: ").append(target.toAbsolutePath()).append("\n\n");

        try (Stream<Path> stream = Files.list(target)) {
            List<Path> entries = stream.sorted().toList();
            for (Path entry : entries) {
                boolean isDir = Files.isDirectory(entry);
                long size = isDir ? 0 : Files.size(entry);
                String sizeStr = isDir ? "[DIR]" : formatSize(size);
                sb.append(String.format("  %s  %s%n", sizeStr, entry.getFileName()));
            }
        }

        return new ToolResult(null, NAME, sb.toString(), true, null,
                Map.of("entryCount", entriesCount(target)));
    }

    private long entriesCount(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.count();
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return String.format("%.1fKB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1fMB", bytes / (1024.0 * 1024));
        return String.format("%.1fGB", bytes / (1024.0 * 1024 * 1024));
    }
}
