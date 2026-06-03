package com.springclaw.core;

import java.util.Map;
import java.util.Set;

/**
 * Policy for controlling which tools an agent can use.
 *
 * <p>The policy uses allowlists and denylists:
 * <ul>
 *   <li>If the global allowlist is empty, all tools are allowed by default.</li>
 *   <li>If the global allowlist is non-empty, only listed tools are allowed.</li>
 *   <li>Denylist always takes precedence — denied tools are never allowed.</li>
 *   <li>Agent-specific lists override global lists.</li>
 * </ul>
 */
public interface ToolPolicy {

    /**
     * Check if a tool is allowed for a given agent.
     *
     * @param tool    the tool to check
     * @param agentId the agent requesting the tool
     * @return true if the tool is allowed
     */
    boolean isToolAllowed(ToolDefinition tool, String agentId);

    /**
     * Set global allowlist. When present, only tools in these sets are allowed.
     * Map key: agent ID (use "*" for all agents), value: set of allowed tool names.
     */
    void setAllowList(Map<String, Set<String>> allowList);

    /**
     * Set global denylist. Tools matching these patterns are never allowed.
     * Map key: agent ID (use "*" for all agents), value: set of denied tool names.
     */
    void setDenyList(Map<String, Set<String>> denyList);

    /**
     * Check if a tool name matches a glob pattern.
     * Supports "*" (any characters) and "?" (single character).
     */
    static boolean matchesGlob(String name, String pattern) {
        return globMatch(name, pattern, 0, 0);
    }

    private static boolean globMatch(String text, String pattern, int ti, int pi) {
        int tLen = text.length();
        int pLen = pattern.length();

        while (ti < tLen) {
            char pc = pattern.charAt(pi);
            if (pc == '?') {
                ti++; pi++;
            } else if (pc == '*') {
                // Skip consecutive stars
                while (pi < pLen && pattern.charAt(pi) == '*') pi++;
                if (pi == pLen) return true; // trailing star matches everything
                for (int t = ti; t <= tLen; t++) {
                    if (globMatch(text, pattern, t, pi)) return true;
                }
                return false;
            } else if (pi < pLen && pc == text.charAt(ti)) {
                ti++; pi++;
            } else {
                return false;
            }
        }

        while (pi < pLen && pattern.charAt(pi) == '*') pi++;
        return pi == pLen;
    }
}
