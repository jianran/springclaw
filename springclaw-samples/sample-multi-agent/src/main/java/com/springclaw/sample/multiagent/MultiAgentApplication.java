package com.springclaw.sample.multiagent;

import com.springclaw.core.*;
import com.springclaw.spring.boot.Agent;
import com.springclaw.spring.boot.EnableSpringClaw;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Multi-agent SpringClaw sample application.
 *
 * <p>Demonstrates multiple agents with different specializations:
 * <ul>
 *   <li>tech — Technical assistant for coding and architecture</li>
 *   <li>creative — Creative assistant for writing and brainstorming</li>
 *   <li>router — Routes queries to the appropriate specialist</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * export OPENAI_API_KEY=sk-xxx
 * ./gradlew :springclaw-samples:sample-multi-agent:bootRun
 *
 * # Chat with the router agent (default)
 * curl -X POST http://localhost:8080/api/agents/router/prompt \
 *   -H "Content-Type: application/json" \
 *   -d '{"message": "How do I implement a binary search in Java?"}'
 *
 * # Chat with tech agent directly
 * curl -X POST http://localhost:8080/api/agents/tech/prompt \
 *   -H "Content-Type: application/json" \
 *   -d '{"message": "Explain microservices vs monolith"}'
 *
 * # List all agents
 * curl http://localhost:8080/api/agents
 * }</pre>
 */
@SpringBootApplication
@EnableSpringClaw
public class MultiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiAgentApplication.class, args);
    }

    /**
     * Technical assistant agent — handles coding, architecture, debugging.
     */
    @Bean
    @Agent("tech")
    public AgentConfig techAgentConfig() {
        return new AgentConfig(
                "tech",
                "Tech Assistant",
                "Expert in software development, architecture, and debugging",
                """
                You are a technical assistant specialized in software development.
                You provide code examples, architecture advice, and debugging help.
                Always prefer clean, well-structured code with clear explanations.
                When discussing technologies, be opinionated but explain trade-offs.
                """,
                new ModelConfig("openai", "gpt-4o", 0.3, null, Map.of()),
                List.of("web_search", "file_read"),
                List.of(),
                new MemoryConfig("in-memory", 100, Map.of()),
                Map.of("role", "tech"),
                null, null
        );
    }

    /**
     * Creative assistant agent — handles writing, brainstorming, design.
     */
    @Bean
    @Agent("creative")
    public AgentConfig creativeAgentConfig() {
        return new AgentConfig(
                "creative",
                "Creative Assistant",
                "Expert in writing, design, and creative tasks",
                """
                You are a creative assistant. You help with writing, brainstorming,
                and creative tasks. Be imaginative, inspiring, and expressive.
                Adapt your tone and style to the user's needs.
                Offer multiple perspectives and alternatives.
                """,
                new ModelConfig("openai", "gpt-4o", 0.9, null, Map.of()),
                List.of("web_search", "web_fetch"),
                List.of(),
                new MemoryConfig("in-memory", 50, Map.of()),
                Map.of("role", "creative"),
                null, null
        );
    }

    /**
     * Router agent — routes queries to specialist agents.
     */
    @Bean
    @Agent("router")
    public AgentConfig routerAgentConfig() {
        return new AgentConfig(
                "router",
                "Router",
                "Routes queries to specialist agents (tech or creative)",
                """
                You are a routing assistant. When users ask about:
                - Code, algorithms, architecture, debugging -> route to the tech agent
                - Writing, brainstorming, design, creative tasks -> route to the creative agent
                - General knowledge -> answer directly

                To route, say "ROUTE_TO_TECH: <message>" or "ROUTE_TO_CREATIVE: <message>".
                """,
                new ModelConfig("openai", "gpt-4o", 0.5, null, Map.of()),
                List.of("web_search"),
                List.of(),
                new MemoryConfig("in-memory", 30, Map.of()),
                Map.of("role", "router"),
                null, null
        );
    }
}
