package com.springclaw.sample.basic;

import com.springclaw.spring.boot.EnableSpringClaw;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Basic SpringClaw sample application.
 *
 * <p>A minimal agent with web channel, tool support, and in-memory chat.
 *
 * <p>Usage:
 * <pre>{@code
 * # Set your API key
 * export OPENAI_API_KEY=sk-xxx
 *
 * # Run the app
 * ./gradlew :springclaw-samples:sample-basic:bootRun
 *
 * # Test with curl
 * curl -X POST http://localhost:8080/api/agents/assistant/prompt \
 *   -H "Content-Type: application/json" \
 *   -d '{"message": "Hello, who are you?"}'
 *
 * # Or open the web chat at http://localhost:8080
 * }</pre>
 */
@SpringBootApplication
@EnableSpringClaw
public class BasicApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicApplication.class, args);
    }
}
