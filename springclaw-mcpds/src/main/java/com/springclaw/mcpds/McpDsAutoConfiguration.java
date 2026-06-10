package com.springclaw.mcpds;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for MCP-DS (Model Context Protocol - Discovery Service).
 *
 * <p>Registers the MCP-DS client, trust evaluator, and server resolver beans
 * when the feature is enabled (enabled by default).</p>
 *
 * @author SpringClaw
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "springclaw.mcpds", name = "enabled", havingValue = "true", matchIfMissing = true)
public class McpDsAutoConfiguration {

    @Bean
    McpDsProperties mcpDsProperties() {
        return new McpDsProperties();
    }

    @Bean
    McpDsClient mcpDsClient(McpDsProperties properties) {
        List<String> registries = properties.getRegistries();
        String baseUrl = !registries.isEmpty() ? registries.get(0) : "https://registry.mcpds.io";
        return new McpDsClient(baseUrl);
    }

    @Bean
    TrustEvaluator trustEvaluator() {
        return new TrustEvaluator();
    }

    @Bean
    McpDsResolver mcpDsResolver(McpDsClient client, TrustEvaluator evaluator, McpDsProperties properties) {
        return new McpDsResolver(client, evaluator, properties.getTrustMin());
    }
}
