package com.springclaw.memory;

import com.springclaw.spring.boot.SpringClawProperties;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for conversation memory.
 *
 * <p>Creates a ChatMemory bean based on springclaw.memory configuration.
 * Default type is "in-memory".
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "springclaw.memory", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MemoryAutoConfiguration {

    @Bean
    public ChatMemory chatMemory(SpringClawProperties properties) {
        return ChatMemoryStoreFactory.create(properties.getMemory());
    }
}
