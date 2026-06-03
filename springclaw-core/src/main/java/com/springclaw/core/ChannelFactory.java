package com.springclaw.core;

/**
 * Factory for creating channel adapters, used by plugins.
 *
 * @see PluginAPI#registerChannelFactory(ChannelFactory)
 */
public interface ChannelFactory {

    /**
     * Create a channel adapter for the given context.
     *
     * @param context the plugin context
     * @return the channel adapter
     */
    ChannelAdapter create(PluginContext context);

    /** Unique identifier for this factory. */
    String getId();
}
