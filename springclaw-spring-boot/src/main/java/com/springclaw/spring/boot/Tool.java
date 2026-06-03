package com.springclaw.spring.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a method as a SpringClaw tool.
 *
 * <p>Use on methods within Spring beans to expose them as agent tools:
 * <pre>{@code
 * @Component
 * public class WeatherService {
 *     @Tool(description = "Get current weather for a city")
 *     public String getWeather(String city) {
 *         return "Sunny, 22C in " + city;
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tool {

    /** Tool name (used to reference the tool). */
    String value() default "";

    /** Tool description shown to the LLM. */
    String description() default "";
}
