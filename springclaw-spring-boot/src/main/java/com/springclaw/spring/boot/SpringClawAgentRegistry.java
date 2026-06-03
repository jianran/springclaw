package com.springclaw.spring.boot;

import com.springclaw.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that manages all SpringClaw agents in the application.
 *
 * <p>Agents are created from two sources:
 * <ol>
 *   <li>SpringClawProperties.agent map (YAML/properties configuration)</li>
 *   <li>{@code @Agent}-annotated {@code @Bean} methods</li>
 * </ol>
 */
public class SpringClawAgentRegistry implements AgentRegistry {

    private static final Logger log = LoggerFactory.getLogger(SpringClawAgentRegistry.class);

    private final SpringClawProperties properties;
    private final ApplicationContext context;
    private final List<Hook> hooks;
    private final Map<String, com.springclaw.core.Agent> agents = new ConcurrentHashMap<>();

    public SpringClawAgentRegistry(SpringClawProperties properties, ApplicationContext context, List<Hook> hooks) {
        this.properties = properties;
        this.context = context;
        this.hooks = hooks;
        registerFromProperties();
        registerFromBeans();
        log.info("SpringClawAgentRegistry initialized with {} agents: {}", agents.size(), agents.keySet());
    }

    private void registerFromProperties() {
        if (properties.getAgents() == null || properties.getAgents().isEmpty()) {
            return;
        }
        properties.getAgents().forEach((id, agentProps) -> {
            if (!agents.containsKey(id)) {
                com.springclaw.core.Agent agent = createAgent(id, agentProps);
                agents.put(id, agent);
                log.info("Registered agent '{}' from properties", id);
            }
        });
    }

    private void registerFromBeans() {
        // Find @Agent-annotated beans
        Map<String, Object> beans = context.getBeansWithAnnotation(Agent.class);
        beans.forEach((name, bean) -> {
            if (bean instanceof AgentConfig config) {
                String id = config.id();
                if (!agents.containsKey(id)) {
                    com.springclaw.core.Agent agent = createAgent(config);
                    agents.put(id, agent);
                    log.info("Registered agent '{}' from bean @Agent({})", id, name);
                }
            }
        });
    }

    private com.springclaw.core.Agent createAgent(String id, AgentProperties props) {
        AgentConfig config = buildConfig(id, props);
        return new SpringClawAgent(config, context, hooks);
    }

    private com.springclaw.core.Agent createAgent(AgentConfig config) {
        return new SpringClawAgent(config, context, hooks);
    }

    private AgentConfig buildConfig(String id, AgentProperties props) {
        String name = props.getName() != null ? props.getName() : id;
        String description = props.getDescription() != null ? props.getDescription() : "";
        String systemPrompt = props.getSystemPrompt() != null ? props.getSystemPrompt() : "";

        ModelConfig model = new ModelConfig(
                props.getModel().getProvider(),
                props.getModel().getModelId(),
                props.getModel().getTemperature(),
                props.getModel().getMaxTokens(),
                props.getModel().getExtraOptions()
        );

        MemoryConfig memory = new MemoryConfig(
                props.getMemory().getType(),
                props.getMemory().getMaxMessages(),
                props.getMemory().getExtra()
        );

        return new AgentConfig(
                id, name, description, systemPrompt, model,
                props.getTools().getAllow(),
                props.getTools().getDeny(),
                memory,
                props.getMetadata(),
                null, null
        );
    }

    @Override
    public com.springclaw.core.Agent getAgent(String id) {
        return agents.get(id);
    }

    @Override
    public com.springclaw.core.Agent getAgentOrThrow(String id) {
        com.springclaw.core.Agent agent = agents.get(id);
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found: " + id);
        }
        return agent;
    }

    @Override
    public Collection<com.springclaw.core.Agent> getAllAgents() {
        return Collections.unmodifiableCollection(agents.values());
    }

    @Override
    public com.springclaw.core.Agent registerAgent(AgentConfig config) {
        com.springclaw.core.Agent agent = new SpringClawAgent(config, context, hooks);
        agents.put(config.id(), agent);
        log.info("Dynamically registered agent '{}'", config.id());
        return agent;
    }

    @Override
    public void unregisterAgent(String id) {
        com.springclaw.core.Agent removed = agents.remove(id);
        if (removed != null) {
            log.info("Unregistered agent '{}'", id);
        }
    }

    List<Hook> getHooks() {
        return hooks;
    }
}
