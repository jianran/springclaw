package com.springclaw.sample.cli;

import com.springclaw.spring.boot.EnableSpringClaw;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringClaw CLI sample application.
 *
 * <p>A minimal agent configured for use with the CLI.
 * Supports both interactive TUI and command-line modes.
 *
 * <p>Usage:
 * <pre>{@code
 * # Set your API key
 * export OPENAI_API_KEY=sk-xxx
 *
 * # Interactive mode (default)
 * ./gradlew :springclaw-samples:sample-cli:bootRun
 *
 * # One-shot prompt
 * ./gradlew :springclaw-samples:sample-cli:bootRun -- args send "What is AI?"
 *
 * # List agents
 * ./gradlew :springclaw-samples:sample-cli:bootRun -- args agents
 * }</pre>
 */
@SpringBootApplication
@EnableSpringClaw
public class CliSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CliSampleApplication.class, args);
    }
}
