package com.springclaw.gateway;

import com.springclaw.spring.boot.GatewayProperties;
import com.springclaw.spring.boot.AgentRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

/**
 * Auto-configuration for the Gateway module.
 *
 * <p>Enabled when springclaw.gateway.enabled is true (default).
 * Disables entirely with springclaw.gateway.enabled=false.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "springclaw.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayAutoConfiguration {

    @Bean
    public ChannelManager channelManager(AgentRegistry agentRegistry) {
        return new ChannelManager(agentRegistry);
    }

    @Bean
    public GatewayServer gatewayServer(GatewayProperties properties, ChannelManager channelManager) {
        return new GatewayServerImpl(properties, channelManager);
    }

    @Bean
    public GatewayController gatewayController(AgentRegistry agentRegistry) {
        return new GatewayController(agentRegistry);
    }

    @Bean
    public WebSocketSessionHandler webSocketSessionHandler(AgentRegistry agentRegistry) {
        return new WebSocketSessionHandler(agentRegistry);
    }
}
