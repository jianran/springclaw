package com.springclaw.spring.boot;

import com.springclaw.core.*;
import org.springframework.ai.model.function.FunctionCallback;

import java.util.Map;

/**
 * Adapter that bridges Spring AI's FunctionCallback to SpringClaw's ToolDefinition.
 */
class SpringClawToolAdapter implements ToolDefinition {

    private final FunctionCallback delegate;

    SpringClawToolAdapter(FunctionCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String getSchema() {
        return delegate.getInputTypeSchema();
    }

    @Override
    public ToolExecutionMode getExecutionMode() {
        return ToolExecutionMode.SEQUENTIAL;
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments, ToolContext context) {
        try {
            String result = delegate.call(arguments.toString());
            return new ToolResult(
                    null,
                    getName(),
                    result,
                    true,
                    null,
                    context != null ? Map.of("agentId", context.agentId()) : Map.of()
            );
        } catch (Exception e) {
            return new ToolResult(
                    null,
                    getName(),
                    "Error executing tool: " + e.getMessage(),
                    false,
                    e.getMessage(),
                    Map.of()
            );
        }
    }

    @Override
    public boolean isAllowed(String agentId, ToolPolicy policy) {
        return policy.isToolAllowed(this, agentId);
    }
}
