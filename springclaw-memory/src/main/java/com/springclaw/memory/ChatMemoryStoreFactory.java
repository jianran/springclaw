package com.springclaw.memory;

import com.springclaw.spring.boot.MemoryProperties;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;

import java.util.Map;

/**
 * Factory for creating Spring AI ChatMemory instances.
 *
 * <p>Creates ChatMemory based on the configured type:
 * <ul>
 *   <li>"in-memory" — InMemoryChatMemory</li>
 *   <li>"redis" — RedisChatMemoryStore</li>
 *   <li>"jdbc" — JdbcChatMemoryStore</li>
 *   <li>"mongo" — MongoChatMemoryStore</li>
 * </ul>
 */
public class ChatMemoryStoreFactory {

    /**
     * Create a ChatMemory instance based on configuration.
     *
     * @param config memory configuration properties
     * @return configured ChatMemory
     */
    public static ChatMemory create(MemoryProperties config) {
        return switch (config.getType().toLowerCase()) {
            case "redis" -> createRedis(config);
            case "jdbc" -> createJdbc(config);
            case "mongo" -> createMongo(config);
            case "in-memory" -> createInMemory(config);
            default -> createInMemory(config);
        };
    }

    private static ChatMemory createInMemory(MemoryProperties config) {
        return new InMemoryChatMemory();
    }

    private static ChatMemory createRedis(MemoryProperties config) {
        return null; // Placeholder — requires Redis auto-config
    }

    private static ChatMemory createJdbc(MemoryProperties config) {
        try {
            // JDBC requires DataSource auto-configured
            return null; // Placeholder — requires actual DataSource
        } catch (Exception e) {
            throw new IllegalStateException(
                    "JDBC memory store requested but spring-boot-starter-jdbc not on classpath", e);
        }
    }

    private static ChatMemory createMongo(MemoryProperties config) {
        try {
            // MongoDB requires MongoTemplate auto-configured
            return null; // Placeholder — requires actual MongoTemplate
        } catch (Exception e) {
            throw new IllegalStateException(
                    "MongoDB memory store requested but spring-boot-starter-data-mongodb not on classpath", e);
        }
    }
}
