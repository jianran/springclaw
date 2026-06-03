package com.springclaw.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Context passed to plugin factories during tool/channel creation.
 *
 * @param pluginId    the ID of the plugin requesting the tool/channel
 * @param config      plugin-specific configuration from application properties
 * @param agentConfig the agent configuration the tool/channel is being created for
 */
public record PluginContext(
    String pluginId,
    Map<String, Object> config,
    AgentConfig agentConfig
) {
    public PluginContext {
        if (config == null) config = new HashMap<>();
    }
}
