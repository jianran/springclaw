package com.springclaw.tools;

import com.springclaw.core.ToolContext;
import com.springclaw.core.ToolDefinition;
import com.springclaw.core.ToolExecutionMode;
import com.springclaw.core.ToolResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Web fetch tool — fetches a URL and extracts readable text content.
 *
 * <p>Uses Jsoup to parse HTML and extract clean text.
 * Supports only HTTP(S) protocols with security restrictions.
 */
public class WebFetchTool implements ToolDefinition {

    private static final String NAME = "web_fetch";
    private static final String DESCRIPTION = "Fetch a URL and extract readable text content. Returns the page title and main content as clean text. Only HTTP(S) URLs are supported.";
    private static final String SCHEMA = """
            {
              "type": "object",
              "properties": {
                "url": {
                  "type": "string",
                  "description": "The URL to fetch"
                },
                "maxLength": {
                  "type": "integer",
                  "description": "Maximum characters to return (default 5000)",
                  "default": 5000
                }
              },
              "required": ["url"]
            }
            """;

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

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
        String url = (String) arguments.get("url");
        int maxLength = arguments.containsKey("maxLength")
                ? ((Number) arguments.get("maxLength")).intValue()
                : 5000;

        if (url == null || url.isBlank()) {
            return new ToolResult(null, NAME, "URL is required", false, "Missing URL", Map.of());
        }

        // Security check: only allow http/https
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return new ToolResult(null, NAME, "Only http:// and https:// URLs are supported",
                    false, "Invalid URL scheme", Map.of());
        }

        try {
            URL parsedUrl = new URL(url);

            // Security: block internal/private addresses
            InetAddress[] addresses = InetAddress.getAllByName(parsedUrl.getHost());
            for (InetAddress addr : addresses) {
                if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()
                        || addr.isSiteLocalAddress()) {
                    return new ToolResult(null, NAME,
                            "Access to internal addresses is blocked",
                            false, "Internal address blocked", Map.of());
                }
            }

            Document doc = Jsoup.connect(url)
                    .timeout((int) TIMEOUT.toMillis())
                    .userAgent("SpringClaw/1.0 (AI Agent)")
                    .followRedirects(false)
                    .get();

            // Extract text content
            doc.select("script, style, nav, footer, header, aside").remove();

            String title = doc.title();
            String text = doc.body().text();

            if (text.length() > maxLength) {
                text = text.substring(0, maxLength) + "...";
            }

            String result = "Title: " + title + "\n\n" + text;

            return new ToolResult(null, NAME, result, true, null, Map.of());

        } catch (SecurityException e) {
            return new ToolResult(null, NAME, "Security error: " + e.getMessage(),
                    false, e.getMessage(), Map.of());
        } catch (Exception e) {
            return new ToolResult(null, NAME, "Failed to fetch URL: " + e.getMessage(),
                    false, e.getMessage(), Map.of());
        }
    }
}
