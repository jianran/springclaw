package com.springclaw.spring.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Enable SpringClaw AI Agent Framework in a Spring Boot application.
 *
 * <p>Place this annotation on your main application class or a configuration class:
 * <pre>{@code
 * @SpringBootApplication
 * @EnableSpringClaw
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }</pre>
 *
 * @see SpringClawAutoConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan
@Import(SpringClawAutoConfiguration.class)
public @interface EnableSpringClaw {
}
