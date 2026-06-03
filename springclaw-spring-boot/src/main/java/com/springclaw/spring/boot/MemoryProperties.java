package com.springclaw.spring.boot;

import java.util.HashMap;
import java.util.Map;

/**
 * Conversation memory configuration.
 */
public class MemoryProperties {

    /** Memory backend type: "in-memory", "redis", "jdbc", "mongo". */
    private String type = "in-memory";

    /** Maximum messages to retain per conversation. */
    private int maxMessages = 50;

    /** Backend-specific configuration. */
    private Map<String, Object> extra = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
