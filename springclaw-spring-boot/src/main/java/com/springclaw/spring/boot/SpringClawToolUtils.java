package com.springclaw.spring.boot;

import com.springclaw.core.ToolContext;
import com.springclaw.core.ToolDefinition;
import com.springclaw.core.ToolResult;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for creating Spring AI FunctionCallback instances from SpringClaw tool definitions.
 */
final class SpringClawToolUtils {

    private SpringClawToolUtils() {}

    /**
     * Create a Spring AI FunctionCallbackProvider from an object with @Tool-annotated methods.
     * Uses reflection to find methods annotated with org.springframework.ai.tool.annotation.Tool.
     */
    static ToolCallbackProvider createToolCallbackProvider(Object bean) {
        Class<?> clazz = bean.getClass();
        java.util.List<FunctionCallback> callbacks = new java.util.ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            // Check for Spring AI @Tool annotation
            if (method.isAnnotationPresent(
                    org.springframework.ai.tool.annotation.Tool.class)) {

                FunctionCallback callback = createCallback(bean, method);
                if (callback != null) {
                    callbacks.add(callback);
                }
            }
        }

        if (callbacks.isEmpty()) {
            return null;
        }

        return new ToolCallbackProvider() {
            @Override
            public FunctionCallback[] getToolCallbacks() {
                return callbacks.toArray(new FunctionCallback[0]);
            }
        };
    }

    private static FunctionCallback createCallback(Object bean, Method method) {
        String name = method.getName();
        String desc = method.isAnnotationPresent(org.springframework.ai.tool.annotation.Tool.class)
                ? method.getAnnotation(org.springframework.ai.tool.annotation.Tool.class).description()
                : "";

        return new FunctionCallback() {
            @Override
            public String getName() { return name; }

            @Override
            public String getDescription() { return desc; }

            @Override
            public String getInputTypeSchema() { return "{}"; }

            @Override
            public String call(String toolInput) {
                try {
                    method.setAccessible(true);
                    Object result = method.invoke(bean);
                    return result != null ? result.toString() : "";
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }
        };
    }
}
