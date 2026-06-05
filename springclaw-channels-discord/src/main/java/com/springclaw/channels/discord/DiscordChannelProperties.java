package com.springclaw.channels.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the Discord channel adapter.
 *
 * <p>Example YAML:
 * <pre>{@code
 * springclaw:
 *   channels:
 *     discord:
 *       enabled: true
 *       bot-token: ${DISCORD_BOT_TOKEN}
 *       allowed-users:
 *         - "user-id-1"
 *         - "user-id-2"
 *       allowed-servers:
 *         - "server-id-1"
 * }</pre>
 */
@ConfigurationProperties(prefix = "springclaw.channels.discord")
public class DiscordChannelProperties {

    /** Whether to enable the Discord channel (default false). */
    private boolean enabled = false;

    /** Discord bot token (required when enabled). */
    private String botToken;

    /** List of allowed Discord user IDs (empty = allow all). */
    private List<String> allowedUsers = new ArrayList<>();

    /** List of allowed Discord server/guild IDs (empty = allow all). */
    private List<String> allowedServers = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public List<String> getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(List<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }

    public List<String> getAllowedServers() {
        return allowedServers;
    }

    public void setAllowedServers(List<String> allowedServers) {
        this.allowedServers = allowedServers;
    }
}
