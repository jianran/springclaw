package com.springclaw.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * SpringClaw CLI — a hybrid CLI + TUI application for interacting with AI agents.
 *
 * <p>Usage patterns:
 * <ul>
 *   <li>Interactive mode: {@code java -jar springclaw-cli.jar} — starts the TUI</li>
 *   <li>Command mode: {@code java -jar springclaw-cli.jar -- send "hello"} — runs a command</li>
 *   <li>Remote mode: set {@code springclaw.cli.mode=remote} in config</li>
 * </ul>
 *
 * <p>Can be used as a standalone Spring Boot application or as a library.
 */
@SpringBootApplication
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.springclaw\\.gateway\\..*")
})
public class CliApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CliApplication.class);
        app.setAdditionalProfiles("cli");

        // Check if subcommand arguments are provided
        boolean hasSubcommand = hasSubcommand(args);
        if (hasSubcommand) {
            // Run without starting embedded server
            app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
        }

        app.run(args);
    }

    /**
     * Check if the args contain a subcommand.
     */
    private static boolean hasSubcommand(String[] args) {
        String[] subcommands = {"chat", "send", "agents", "tools", "session"};
        for (String arg : args) {
            for (String sub : subcommands) {
                if (arg.equals(sub)) return true;
            }
        }
        return false;
    }
}
