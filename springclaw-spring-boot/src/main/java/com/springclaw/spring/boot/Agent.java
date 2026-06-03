package com.springclaw.spring.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a Spring bean as a SpringClaw agent configuration.
 *
 * <p>Use on {@code @Bean} methods that return {@code AgentConfig}:
 * <pre>{@code
 * @Bean
 * @Agent("tech")
 * public AgentConfig techAgentConfig() {
 *     return new AgentConfig("tech", "Tech Assistant", ...);
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Agent {

    /** Unique agent identifier. */
    String value();

    /** Optional description override. */
    String description() default "";

    /** Optional system prompt override. */
    String systemPrompt() default "";
}
