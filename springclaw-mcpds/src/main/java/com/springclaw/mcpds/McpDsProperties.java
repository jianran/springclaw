package com.springclaw.mcpds;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for MCP-DS (Model Context Protocol - Discovery Service).
 *
 * <p>Prefix: {@code springclaw.mcpds}
 *
 * <p>See <a href="https://github.com/jianran/mcp-ds">MCP-DS Spec</a>.
 */
@ConfigurationProperties(prefix = "springclaw.mcpds")
public class McpDsProperties {

    /**
     * Whether MCP-DS discovery is enabled.
     */
    private boolean enabled = true;

    /**
     * List of MCP-DS registry URLs to query for server discovery.
     * Default: the public MCP-DS registry.
     */
    private List<String> registries = List.of("https://registry.mcpds.io");

    /**
     * Minimum trust score threshold (0.0 - 1.0) for resolved servers.
     * Servers below this score will be filtered out.
     */
    private double trustMin = 0.5;

    /**
     * Request timeout for MCP-DS API calls.
     */
    private Duration timeout = Duration.ofSeconds(10);

    /**
     * Connection timeout for MCP-DS API calls.
     */
    private Duration connectTimeout = Duration.ofSeconds(5);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getRegistries() {
        return registries;
    }

    public void setRegistries(List<String> registries) {
        this.registries = registries;
    }

    public double getTrustMin() {
        return trustMin;
    }

    public void setTrustMin(double trustMin) {
        this.trustMin = trustMin;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
