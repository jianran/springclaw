package com.springclaw.tools;

import java.util.Map;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for built-in tools.
 *
 * <p>Registers web_search, web_fetch, file_read, cron, and session tools.
 * Disabled with springclaw.tools-enabled=false.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "springclaw", name = "tools-enabled", havingValue = "true", matchIfMissing = true)
public class ToolAutoConfiguration {

    @Bean
    public ToolCallbackProvider webSearchToolProvider() {
        return new ToolCallbackProvider() {
            @Override
            public ToolCallback[] getToolCallbacks() {
                return new ToolCallback[] {
                    wrapTool("web_search", "Search the web for information.", WEB_SEARCH_SCHEMA, args -> {
                        String query = (String) args.get("query");
                        int maxResults = args.containsKey("maxResults")
                                ? ((Number) args.get("maxResults")).intValue()
                                : 5;
                        return "Search query: " + query + "\n(maxResults: " + maxResults + ")\n\n" +
                                "Note: Configure Spring AI Web Search for actual results.\n" +
                                "Add dependency: org.springframework.ai:spring-ai-web-search-spring-boot-starter";
                    })
                };
            }
        };
    }

    @Bean
    public ToolCallbackProvider webFetchToolProvider() {
        return new ToolCallbackProvider() {
            @Override
            public ToolCallback[] getToolCallbacks() {
                return new ToolCallback[] {
                    wrapTool("web_fetch", "Fetch and parse the content of a URL.", WEB_FETCH_SCHEMA, args -> {
                        String url = (String) args.get("url");
                        return "Note: Web fetch is not configured. To enable, add spring-ai-web-fetch dependency. URL: " + url;
                    })
                };
            }
        };
    }

    @Bean
    public ToolCallbackProvider fileReadToolProvider() {
        return new ToolCallbackProvider() {
            @Override
            public ToolCallback[] getToolCallbacks() {
                return new ToolCallback[] {
                    wrapTool("file_read", "Read the contents of a file.", FILE_READ_SCHEMA, args -> {
                        String path = (String) args.get("path");
                        return "Note: File read is not fully configured. Path: " + path;
                    })
                };
            }
        };
    }

    @Bean
    public ToolCallbackProvider cronToolProvider() {
        return new ToolCallbackProvider() {
            @Override
            public ToolCallback[] getToolCallbacks() {
                return new ToolCallback[] {
                    wrapTool("cron_job", "Manage scheduled cron jobs.", CRON_SCHEMA, args -> {
                        String action = (String) args.get("action");
                        return "Note: Cron job not fully configured. Action: " + action;
                    })
                };
            }
        };
    }

    @Bean
    public ToolCallbackProvider sessionToolProvider() {
        return new ToolCallbackProvider() {
            @Override
            public ToolCallback[] getToolCallbacks() {
                return new ToolCallback[] {
                    wrapTool("session_info", "Get information about the current session.", SESSION_SCHEMA, args -> {
                        String sessionId = (String) args.get("sessionId");
                        return "Note: Session info not fully configured. Session: " + sessionId;
                    })
                };
            }
        };
    }

    @FunctionalInterface
    private interface ToolFn {
        String apply(Map<String, Object> args);
    }

    private ToolCallback wrapTool(String name, String description, String schema, ToolFn fn) {
        return FunctionToolCallback.builder(name, args -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) args;
            return fn.apply(map);
        })
                .description(description)
                .inputSchema(schema)
                .inputType(Map.class)
                .build();
    }

    private static final String WEB_SEARCH_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "query": { "type": "string", "description": "The search query" },
                "maxResults": { "type": "integer", "description": "Maximum number of results (1-10)", "default": 5 }
              },
              "required": ["query"]
            }""";

    private static final String WEB_FETCH_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "url": { "type": "string", "description": "The URL to fetch" }
              },
              "required": ["url"]
            }""";

    private static final String FILE_READ_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "path": { "type": "string", "description": "File path to read" }
              },
              "required": ["path"]
            }""";

    private static final String CRON_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "action": { "type": "string", "description": "Cron action to perform" }
              },
              "required": ["action"]
            }""";

    private static final String SESSION_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "sessionId": { "type": "string", "description": "Session ID to query" }
              },
              "required": ["sessionId"]
            }""";
}
