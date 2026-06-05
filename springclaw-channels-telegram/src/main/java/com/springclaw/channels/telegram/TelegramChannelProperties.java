package com.springclaw.channels.telegram;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the Telegram channel adapter.
 *
 * <p>Two operating modes are supported:
 * <ul>
 *   <li><b>polling</b> (default): Simple long-polling, no public URL needed</li>
 *   <li><b>webhook</b>: Requires HTTPS endpoint and webhook URL</li>
 * </ul>
 *
 * <p>Example YAML (long polling):
 * <pre>{@code
 * springclaw:
 *   channels:
 *     telegram:
 *       enabled: true
 *       mode: polling
 *       bot-token: ${TELEGRAM_BOT_TOKEN}
 *       bot-name: "MyClawBot"
 *       allowed-users:
 *         - "123456789"
 * }</pre>
 *
 * <p>Example YAML (webhook):
 * <pre>{@code
 * springclaw:
 *   channels:
 *     telegram:
 *       enabled: true
 *       mode: webhook
 *       bot-token: ${TELEGRAM_BOT_TOKEN}
 *       bot-name: "MyClawBot"
 *       webhook-url: "https://example.com/webhook/telegram"
 * }</pre>
 */
@ConfigurationProperties(prefix = "springclaw.channels.telegram")
public class TelegramChannelProperties {

    /** Whether to enable the Telegram channel (default false). */
    private boolean enabled = false;

    /** Operating mode: "polling" (default) or "webhook". */
    private String mode = "polling";

    /** Telegram bot token (required when enabled). */
    private String botToken;

    /** Telegram bot username (shown as @username). */
    private String botName = "SpringClawBot";

    /** Webhook URL (required when mode=webhook). */
    private String webhookUrl;

    /** List of allowed Telegram user IDs (empty = allow all). */
    private List<String> allowedUsers = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public List<String> getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(List<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }
}
