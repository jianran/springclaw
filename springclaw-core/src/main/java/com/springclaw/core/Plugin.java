package com.springclaw.core;

/**
 * A plugin that extends the SpringClaw framework.
 *
 * <p>Plugins register tools, hooks, channels, and services through the PluginAPI.
 * They are the primary extension mechanism in SpringClaw.
 *
 * @see PluginAPI
 * @see PluginContext
 */
public interface Plugin {

    /** Unique identifier for this plugin. */
    String getId();

    /**
     * Register the plugin's extensions with the framework.
     *
     * @param api the registration API provided by the framework
     */
    void register(PluginAPI api);
}
