package com.springclaw.gateway;

import com.springclaw.core.ChannelAdapter;
import com.springclaw.spring.boot.GatewayProperties;
import com.springclaw.spring.boot.AgentRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Collection;

/**
 * Auto-configuration for the Gateway module.
 *
 * <p>Enabled when springclaw.gateway.enabled is true (default).
 * Disables entirely with springclaw.gateway.enabled=false.
 *
 * <p>Auto-registers all ChannelAdapter beans (Web, Discord, Telegram, etc.)
 * with the ChannelManager.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "springclaw.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayAutoConfiguration {

    @Bean
    public ChannelManager channelManager(AgentRegistry agentRegistry) {
        return new ChannelManager(agentRegistry);
    }

    /**
     * Register all ChannelAdapter beans with the ChannelManager.
     *
     * <p>Spring resolves all bean dependencies before initializing beans,
     * so all channel adapter beans (Web, Discord, Telegram) will already
     * exist when this method runs. Only enabled channels are registered
     * (due to @ConditionalOnProperty on their auto-configurations).
     */
    @Bean
    public ChannelAdapterRegistrar channelAdapterRegistrar(
            ChannelManager channelManager,
            Collection<ChannelAdapter> channelAdapters
    ) {
        for (ChannelAdapter adapter : channelAdapters) {
            channelManager.registerChannel(adapter);
        }
        return new ChannelAdapterRegistrar();
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

    /**
     * Marker class to hold the registration bean.
     */
    public static class ChannelAdapterRegistrar {}
}
