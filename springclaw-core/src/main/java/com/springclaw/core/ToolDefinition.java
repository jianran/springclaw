package com.springclaw.core;

/**
 * A tool that an Agent can use during conversation.
 *
 * <p>Tools are functions the LLM can call to perform external actions
 * (search the web, read files, execute code, etc.).
 *
 * @see ToolCall
 * @see ToolResult
 */
public interface ToolDefinition {

    /** Unique name of the tool. */
    String getName();

    /** Human-readable description of what the tool does. */
    String getDescription();

    /** JSON Schema string describing the tool's parameters. */
    String getSchema();

    /** How this tool should be executed relative to others. */
    ToolExecutionMode getExecutionMode();

    /**
     * Execute the tool with the given arguments.
     *
     * @param arguments tool parameters
     * @param context   execution context
     * @return the tool result
     */
    ToolResult execute(java.util.Map<String, Object> arguments, ToolContext context);

    /**
     * Check if this tool is allowed for the given agent under the current policy.
     *
     * @param agentId the agent requesting the tool
     * @param policy  the tool policy to check against
     * @return true if the tool is allowed
     */
    default boolean isAllowed(String agentId, ToolPolicy policy) {
        return policy.isToolAllowed(this, agentId);
    }
}
