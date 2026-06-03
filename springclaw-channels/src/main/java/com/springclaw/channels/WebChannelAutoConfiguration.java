package com.springclaw.channels;

import com.springclaw.spring.boot.AgentRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the web channel adapter.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "springclaw.gateway", name = "web-channel-enabled", havingValue = "true", matchIfMissing = true)
public class WebChannelAutoConfiguration {

    @Bean
    public WebChannelAdapter webChannelAdapter(AgentRegistry agentRegistry) {
        return new WebChannelAdapter(agentRegistry);
    }
}
