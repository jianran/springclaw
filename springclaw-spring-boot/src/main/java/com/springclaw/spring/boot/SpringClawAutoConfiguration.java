package com.springclaw.spring.boot;

import com.springclaw.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main auto-configuration for SpringClaw.
 *
 * <p>Registers the core beans: AgentRegistry, ToolRegistry, and HookRegistry.
 * Agents are created from both SpringClawProperties and {@code @Agent}-annotated beans.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "springclaw", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ SpringClawProperties.class, GatewayProperties.class })
public class SpringClawAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SpringClawAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(AgentRegistry.class)
    public AgentRegistry agentRegistry(
            SpringClawProperties properties,
            ApplicationContext context
    ) {
        log.info("Initializing SpringClaw AgentRegistry");
        List<Hook> hooks = context.getBeansOfType(Hook.class).values().stream().toList();
        return new SpringClawAgentRegistry(properties, context, hooks);
    }

    @Bean
    @ConditionalOnMissingBean(ToolRegistry.class)
    public ToolRegistry toolRegistry(AgentRegistry agentRegistry) {
        log.info("Initializing SpringClaw ToolRegistry");
        return new SpringClawToolRegistry(agentRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(HookRegistry.class)
    public HookRegistry hookRegistry(SpringClawProperties properties, ApplicationContext context) {
        log.info("Initializing SpringClaw HookRegistry");
        List<Hook> hooks = context.getBeansOfType(Hook.class).values().stream().toList();
        return new SpringClawHookRegistry(hooks, properties.isHooksEnabled());
    }

    @Bean
    @ConditionalOnMissingBean(ToolPolicy.class)
    public ToolPolicy toolPolicy() {
        return new DefaultToolPolicy();
    }

    @Bean
    @ConditionalOnMissingBean(ToolCallbackProvider.class)
    public List<ToolCallbackProvider> scanToolBeans(ApplicationContext context) {
        List<ToolCallbackProvider> providers = new ArrayList<>();
        // Scan for @Tool-annotated methods in Spring beans
        Map<String, Object> beans = context.getBeansWithAnnotation(org.springframework.stereotype.Component.class);
        beans.values().forEach(bean -> {
            Object provider = SpringClawToolUtils.createToolCallbackProvider(bean);
            if (provider instanceof ToolCallbackProvider tcp) {
                providers.add(tcp);
            }
        });
        return providers;
    }
}
