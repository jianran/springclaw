package com.springclaw.core;

import java.util.List;

/**
 * Factory for creating tool definitions, used by plugins.
 *
 * @see PluginAPI#registerToolFactory(ToolFactory)
 */
public interface ToolFactory {

    /**
     * Create tool definitions for the given context.
     *
     * @param context the plugin context with configuration
     * @return list of tool definitions to register
     */
    List<ToolDefinition> create(PluginContext context);

    /** Unique identifier for this factory. */
    String getId();
}
