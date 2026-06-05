package com.springclaw.channels.telegram;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the Telegram channel adapter.
 *
 * <p>Activated when springclaw.channels.telegram.enabled=true.
 * Registered with ChannelManager by GatewayAutoConfiguration.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "springclaw.channels.telegram", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(TelegramChannelProperties.class)
public class TelegramChannelAutoConfiguration {

    @Bean
    public TelegramChannelAdapter telegramChannelAdapter(TelegramChannelProperties properties) {
        return new TelegramChannelAdapter(properties);
    }
}
