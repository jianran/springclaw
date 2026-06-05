package com.springclaw.channels.discord;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the Discord channel adapter.
 *
 * <p>Activated when springclaw.channels.discord.enabled=true.
 * Registered with ChannelManager by GatewayAutoConfiguration.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "springclaw.channels.discord", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DiscordChannelProperties.class)
public class DiscordChannelAutoConfiguration {

    @Bean
    public DiscordChannelAdapter discordChannelAdapter(DiscordChannelProperties properties) {
        return new DiscordChannelAdapter(properties);
    }
}
