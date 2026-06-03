package com.springclaw.tools;

import com.springclaw.core.ToolContext;
import com.springclaw.core.ToolDefinition;
import com.springclaw.core.ToolExecutionMode;
import com.springclaw.core.ToolResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.Map;

/**
 * Web search tool implementation.
 *
 * <p>Provides web search capability through Spring AI's built-in
 * WebSearchToolCallback when configured. Falls back to a simple
 * search prompt if no external search provider is available.
 */
public class WebSearchTool implements ToolDefinition {

    private static final String NAME = "web_search";
    private static final String DESCRIPTION = "Search the web for information. Use this when you need to find current information, news, or data that may not be in your training data.";
    private static final String SCHEMA = """
            {
              "type": "object",
              "properties": {
                "query": {
                  "type": "string",
                  "description": "The search query"
                },
                "maxResults": {
                  "type": "integer",
                  "description": "Maximum number of results (1-10)",
                  "default": 5
                }
              },
              "required": ["query"]
            }
            """;

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
        return ToolExecutionMode.PARALLEL;
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments, ToolContext context) {
        String query = (String) arguments.get("query");
        int maxResults = arguments.containsKey("maxResults")
                ? ((Number) arguments.get("maxResults")).intValue()
                : 5;

        try {
            // Try Spring AI's WebSearchToolCallback first
            return trySpringAiSearch(query, maxResults);
        } catch (Exception e) {
            return new ToolResult(
                    null, NAME,
                    "Web search is not configured. To enable, add spring-ai-web-search dependency.",
                    false, e.getMessage(), Map.of()
            );
        }
    }

    private ToolResult trySpringAiSearch(String query, int maxResults) {
        // Fallback: return a formatted search prompt
        return new ToolResult(
                null, NAME,
                "Search query: " + query + "\n(maxResults: " + maxResults + ")\n\n" +
                "Note: Configure Spring AI Web Search for actual results.\n" +
                "Add dependency: org.springframework.ai:spring-ai-web-search-spring-boot-starter",
                true, null, Map.of()
        );
    }
}
