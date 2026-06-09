package com.springclaw.cli.config;

import com.springclaw.cli.CliProperties;
import com.springclaw.cli.client.AgentClient;
import com.springclaw.cli.client.LocalAgentClient;
import com.springclaw.cli.client.RemoteAgentClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springclaw.spring.boot.AgentRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the SpringClaw CLI.
 *
 * <p>Wires up the appropriate AgentClient implementation based on
 * the configured mode (local or remote).
 */
@AutoConfiguration
@EnableConfigurationProperties(CliProperties.class)
public class CliAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "springclaw.cli", name = "mode", havingValue = "local", matchIfMissing = true)
    public AgentClient localAgentClient(AgentRegistry agentRegistry) {
        return new LocalAgentClient(agentRegistry);
    }

    @Bean
    @ConditionalOnProperty(prefix = "springclaw.cli", name = "mode", havingValue = "remote")
    public AgentClient remoteAgentClient(CliProperties properties, ObjectMapper objectMapper) {
        return new RemoteAgentClient(properties.getGatewayUrl(), objectMapper);
    }
}
