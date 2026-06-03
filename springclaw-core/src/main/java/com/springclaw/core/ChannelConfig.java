package com.springclaw.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for a channel adapter.
 *
 * @param id       channel identifier
 * @param settings channel-specific settings (API keys, endpoints, etc.)
 */
public record ChannelConfig(
    String id,
    Map<String, Object> settings
) {
    public ChannelConfig {
        if (settings == null) settings = new HashMap<>();
    }
}
