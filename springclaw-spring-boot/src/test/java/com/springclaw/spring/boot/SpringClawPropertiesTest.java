package com.springclaw.spring.boot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpringClawPropertiesTest {

    @Test
    void defaultValues() {
        SpringClawProperties props = new SpringClawProperties();
        assertTrue(props.isEnabled());
        assertTrue(props.isHooksEnabled());
        assertEquals(8080, props.getGateway().getPort());
        assertEquals("in-memory", props.getMemory().getType());
        assertEquals(50, props.getMemory().getMaxMessages());
        assertTrue(props.getAgents().isEmpty());
    }

    @Test
    void agentPropertiesDefaults() {
        AgentProperties agent = new AgentProperties();
        assertNull(agent.getName());
        assertNull(agent.getSystemPrompt());
        assertEquals("openai", agent.getModel().getProvider());
        assertEquals("gpt-4o", agent.getModel().getModelId());
    }
}
