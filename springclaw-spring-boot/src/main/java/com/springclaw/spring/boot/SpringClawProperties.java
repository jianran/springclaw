package com.springclaw.spring.boot;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for SpringClaw.
 *
 * <p>Example YAML configuration:
 * <pre>{@code
 * springclaw:
 *   agents:
 *     assistant:
 *       name: "My Assistant"
 *       system-prompt: "You are helpful."
 *       model:
 *         provider: openai
 *         model-id: gpt-4o
 *   gateway:
 *     port: 8080
 * }</pre>
 */
@ConfigurationProperties("springclaw")
public class SpringClawProperties {

    /** Map of agent ID to agent configuration. */
    private Map<String, AgentProperties> agents = new LinkedHashMap<>();

    /** Gateway configuration. */
    private GatewayProperties gateway = new GatewayProperties();

    /** Global memory configuration (used when agent doesn't specify). */
    private MemoryProperties memory = new MemoryProperties();

    /** Whether to enable hook processing (default true). */
    private boolean hooksEnabled = true;

    /** Whether to enable the framework entirely (default true). */
    private boolean enabled = true;

    public Map<String, AgentProperties> getAgents() {
        return agents;
    }

    public void setAgents(Map<String, AgentProperties> agents) {
        this.agents = agents;
    }

    public GatewayProperties getGateway() {
        return gateway;
    }

    public void setGateway(GatewayProperties gateway) {
        this.gateway = gateway;
    }

    public MemoryProperties getMemory() {
        return memory;
    }

    public void setMemory(MemoryProperties memory) {
        this.memory = memory;
    }

    public boolean isHooksEnabled() {
        return hooksEnabled;
    }

    public void setHooksEnabled(boolean hooksEnabled) {
        this.hooksEnabled = hooksEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
