package com.springclaw.channels.telegram;

import com.springclaw.core.ChannelAdapter;
import com.springclaw.core.ChannelConfig;
import com.springclaw.core.InboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.Map;

/**
 * Telegram channel adapter using TelegramBotsApi.
 *
 * <p>Supports two operating modes:
 * <ul>
 *   <li><b>Long polling</b>: Simple setup, no public URL required (default)</li>
 *   <li><b>Webhook</b>: Production-ready, requires HTTPS endpoint</li>
 * </ul>
 *
 * <p>Session management uses Telegram's chat-based model:
 * each chat (private or group) gets its own conversation session.
 */
public class TelegramChannelAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(TelegramChannelAdapter.class);

    private final TelegramChannelProperties properties;
    private final Sinks.Many<InboundMessage> inboundSink = Sinks.many().multicast().onBackpressureBuffer();
    private volatile TelegramLongPollingBot bot;
    private volatile DefaultBotSession session;
    private volatile boolean isRunning = false;
    private volatile ChannelConfig config;

    public TelegramChannelAdapter(TelegramChannelProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getId() {
        return "telegram";
    }

    @Override
    public String getName() {
        return "Telegram Channel";
    }

    @Override
    public void initialize(ChannelConfig config) {
        this.config = config;
        log.info("Telegram channel initialized in {} mode with {} allowed users",
                properties.getMode(),
                properties.getAllowedUsers() != null ? properties.getAllowedUsers().size() : 0);
    }

    @Override
    public Mono<Void> start() {
        return Mono.create(sink -> {
            try {
                DefaultBotOptions options = new DefaultBotOptions();

                // Create the bot instance
                bot = createBot(options);

                if ("webhook".equals(properties.getMode())) {
                    // In webhook mode, the webhook URL is set externally
                    // We just need to create the bot to handle incoming webhooks
                    log.info("Telegram webhook mode configured, URL: {}", properties.getWebhookUrl());
                }

                // Start long polling session
                session = new DefaultBotSession();
                session.setToken(properties.getBotToken());
                session.setCallback(bot);
                // Session name is auto-derived from bot token
                session.start();

                isRunning = true;
                log.info("Telegram channel started as @{}", properties.getBotName());
                sink.success();
            } catch (Exception e) {
                sink.error(new RuntimeException("Failed to start Telegram channel: " + e.getMessage(), e));
            }
        });
    }

    @Override
    public Mono<Void> stop() {
        return Mono.fromRunnable(() -> {
            if (session != null && session.isRunning()) {
                session.stop();
            }
            isRunning = false;
            log.info("Telegram channel stopped");
        });
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public Flux<InboundMessage> receive() {
        return inboundSink.asFlux();
    }

    @Override
    public Mono<Void> send(InboundMessage message, String response) {
        return Mono.create(sink -> {
            try {
                if (bot == null) {
                    log.warn("Telegram bot not running, dropping response for message '{}'", message.id());
                    sink.success();
                    return;
                }

                // Extract chatId from message metadata
                String chatId = (String) message.metadata().getOrDefault("chatId", message.userId());

                SendMessage sendMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text(truncate(response, 4096))
                        .build();

                bot.execute(sendMessage);
                log.debug("Sent Telegram reply to chat {}: {} chars", chatId, response.length());
                sink.success();
            } catch (TelegramApiException e) {
                log.error("Failed to send Telegram reply: {}", e.getMessage());
                sink.error(e);
            }
        });
    }

    /**
     * Process a Telegram update. Called by the session's polling loop.
     */
    public void processUpdate(Update update) {
        if (update == null) {
            return;
        }

        if (!update.hasMessage()) {
            return;
        }

        var message = update.getMessage();
        if (message == null) {
            return;
        }

        // Skip bot messages (from other bots)
        if (message.getFrom() != null && Boolean.TRUE.equals(message.getFrom().getIsBot())) {
            return;
        }

        long chatId = message.getChat().getId();
        var fromUser = message.getFrom();
        Long fromId = fromUser != null ? fromUser.getId() : null;
        String displayName = fromUser != null ?
                (fromUser.getFirstName() != null ? fromUser.getFirstName() : fromUser.getUserName()) : "unknown";
        String content = message.getText();

        if (content == null || content.isBlank()) {
            return;
        }

        // Check allowed users
        if (!isAllowedUser(fromId)) {
            log.debug("Ignoring message from unauthorized Telegram user: {}", fromId);
            return;
        }

        // Create session ID per chat
        String sessionId = "telegram:" + chatId;

        // Create inbound message and emit to sink
        InboundMessage inbound = new InboundMessage(
                null,
                "telegram",
                sessionId,
                fromId != null ? String.valueOf(fromId) : String.valueOf(chatId),
                displayName,
                content,
                Map.of(
                        "chatId", String.valueOf(chatId),
                        "chatType", message.getChat().getType(),
                        "messageId", String.valueOf(message.getMessageId())
                ),
                Instant.now()
        );

        inboundSink.tryEmitNext(inbound);
    }

    /**
     * Create a long-polling bot instance.
     */
    private TelegramLongPollingBot createBot(DefaultBotOptions options) {
        return new TelegramLongPollingBot(options) {
            @Override
            public void onUpdateReceived(Update update) {
                TelegramChannelAdapter.this.processUpdate(update);
            }

            @Override
            public String getBotToken() {
                return properties.getBotToken();
            }

            @Override
            public String getBotUsername() {
                return properties.getBotName();
            }

            @Override
            public void clearWebhook() {
                // No-op for polling mode
            }
        };
    }

    private boolean isAllowedUser(Long userId) {
        if (properties.getAllowedUsers() == null || properties.getAllowedUsers().isEmpty()) {
            return true; // No restriction: allow all
        }
        return properties.getAllowedUsers().contains(String.valueOf(userId));
    }

    private String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 3) + "...";
    }
}
