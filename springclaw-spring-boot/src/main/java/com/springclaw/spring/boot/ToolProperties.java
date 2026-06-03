package com.springclaw.spring.boot;

import java.util.ArrayList;
import java.util.List;

/**
 * Tool policy configuration.
 */
public class ToolProperties {

    /** Allowlist of tool names (empty = allow all). Supports glob patterns (*, ?). */
    private List<String> allow = new ArrayList<>();

    /** Denylist of tool names (takes precedence over allowlist). Supports glob patterns. */
    private List<String> deny = new ArrayList<>();

    public List<String> getAllow() {
        return allow;
    }

    public void setAllow(List<String> allow) {
        this.allow = allow;
    }

    public List<String> getDeny() {
        return deny;
    }

    public void setDeny(List<String> deny) {
        this.deny = deny;
    }
}
