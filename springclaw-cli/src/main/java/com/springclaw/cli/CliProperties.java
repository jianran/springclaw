package com.springclaw.cli;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the SpringClaw CLI application.
 *
 * <p>Controls the CLI's runtime mode (local vs remote),
 * TUI settings, and default agent selection.
 *
 * <p>Example YAML configuration:
 * <pre>{@code
 * springclaw:
 *   cli:
 *     mode: local              # local | remote
 *     gateway-url: ""          # for remote mode
 *     default-agent: "assistant"
 *     tui:
 *       enabled: true
 *       streaming: true
 *       show-token-usage: true
 *       show-tool-calls: true
 * }</pre>
 */
@ConfigurationProperties("springclaw.cli")
public class CliProperties {

    /** Runtime mode: local (in-process agent) or remote (gateway HTTP). */
    private String mode = "local";

    /** Gateway URL for remote mode. */
    private String gatewayUrl = "http://localhost:8080";

    /** Default agent ID to use when none specified. */
    private String defaultAgent;

    /** TUI sub-configuration. */
    private TuiProperties tui = new TuiProperties();

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getDefaultAgent() {
        return defaultAgent;
    }

    public void setDefaultAgent(String defaultAgent) {
        this.defaultAgent = defaultAgent;
    }

    public TuiProperties getTui() {
        return tui;
    }

    public void setTui(TuiProperties tui) {
        this.tui = tui;
    }

    /** TUI sub-configuration properties. */
    public static class TuiProperties {

        /** Whether to enable the TUI (default true). */
        private boolean enabled = true;

        /** Whether to stream responses token-by-token (default true). */
        private boolean streaming = true;

        /** Whether to display token counts after responses (default true). */
        private boolean showTokenUsage = true;

        /** Whether to show tool calls as they execute (default true). */
        private boolean showToolCalls = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isStreaming() {
            return streaming;
        }

        public void setStreaming(boolean streaming) {
            this.streaming = streaming;
        }

        public boolean isShowTokenUsage() {
            return showTokenUsage;
        }

        public void setShowTokenUsage(boolean showTokenUsage) {
            this.showTokenUsage = showTokenUsage;
        }

        public boolean isShowToolCalls() {
            return showToolCalls;
        }

        public void setShowToolCalls(boolean showToolCalls) {
            this.showToolCalls = showToolCalls;
        }
    }
}
