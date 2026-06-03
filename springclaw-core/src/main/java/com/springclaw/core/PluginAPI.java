package com.springclaw.core;

/**
 * API provided to plugins for registering extensions.
 *
 * <p>Plugins call methods on this interface during their {@link Plugin#register(PluginAPI)}
 * phase to add tools, hooks, channels, and services to the framework.
 */
public interface PluginAPI {

    /** Register a factory that creates tool definitions. */
    void registerToolFactory(ToolFactory factory);

    /** Register a lifecycle hook. */
    void registerHook(Hook hook);

    /** Register a factory that creates channel adapters. */
    void registerChannelFactory(ChannelFactory factory);

    /** Register a background service. */
    void registerService(Service service);

    /** Register a prompt pre-processor (runs before the LLM call). */
    void registerPreProcessor(PreProcessor processor);

    /** Register a prompt post-processor (runs after the LLM response). */
    void registerPostProcessor(PostProcessor processor);
}
