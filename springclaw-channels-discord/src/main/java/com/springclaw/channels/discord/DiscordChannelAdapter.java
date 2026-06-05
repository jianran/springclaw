package com.springclaw.channels.discord;

import com.springclaw.core.ChannelAdapter;
import com.springclaw.core.ChannelConfig;
import com.springclaw.core.InboundMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Discord channel adapter using JDA (Java Discord API).
 *
 * <p>Supports three interaction modes:
 * <ul>
 *   <li><b>Mention-based</b>: Users type {@code @BotName message} in any channel</li>
 *   <li><b>Prefix commands</b>: Users type {@code !ask message} in text channels</li>
 *   <li><b>Slash commands</b>: Users use {@code /ask message} or {@code /help}</li>
 * </ul>
 *
 * <p>Session management uses Discord's channel-based model:
 * each text channel gets its own conversation session.
 */
public class DiscordChannelAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscordChannelAdapter.class);
    private static final Pattern MENTION_PATTERN = Pattern.compile("^<@!?>\\d+>\\s*(.*)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^!\\w+\\s*(.*)");

    private final DiscordChannelProperties properties;
    private final Sinks.Many<InboundMessage> inboundSink = Sinks.many().multicast().onBackpressureBuffer();
    private volatile JDA jda;
    private volatile boolean isRunning = false;
    private volatile ChannelConfig config;

    public DiscordChannelAdapter(DiscordChannelProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getId() {
        return "discord";
    }

    @Override
    public String getName() {
        return "Discord Channel";
    }

    @Override
    public void initialize(ChannelConfig config) {
        this.config = config;
        log.info("Discord channel initialized with {} allowed users, {} allowed servers",
                properties.getAllowedUsers() != null ? properties.getAllowedUsers().size() : 0,
                properties.getAllowedServers() != null ? properties.getAllowedServers().size() : 0);
    }

    @Override
    public Mono<Void> start() {
        return Mono.create(sink -> {
            try {
                JDABuilder builder = JDABuilder.createDefault(properties.getBotToken())
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES);

                // Register message listener
                builder.addEventListeners(new ListenerAdapter() {
                    @Override
                    public void onMessageReceived(MessageReceivedEvent event) {
                        handleMessage(event);
                    }
                });

                jda = builder.build();
                jda.awaitReady();

                // Register slash commands
                registerSlashCommands(jda);

                isRunning = true;
                log.info("Discord channel started as {}", jda.getSelfUser().getEffectiveName());
                sink.success();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sink.error(new RuntimeException("Failed to start Discord channel: " + e.getMessage(), e));
            } catch (Exception e) {
                sink.error(new RuntimeException("Failed to start Discord channel: " + e.getMessage(), e));
            }
        });
    }

    @Override
    public Mono<Void> stop() {
        return Mono.fromRunnable(() -> {
            if (jda != null) {
                jda.shutdown();
            }
            isRunning = false;
            log.info("Discord channel stopped");
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
            if (jda == null) {
                log.warn("JDA not initialized, dropping response for message '{}'", message.id());
                sink.success();
                return;
            }

            // Look up the message channel by ID from message metadata
            String channelId = (String) message.metadata().getOrDefault("channelIdRaw", "");
            try {
                MessageChannel channel = jda.getTextChannelById(Long.parseLong(channelId));
                if (channel == null) {
                    // Try as DM channel
                    channel = jda.getPrivateChannelById(Long.parseLong(channelId));
                }
                if (channel == null) {
                    log.warn("Discord channel not found for ID: {}", channelId);
                    sink.success();
                    return;
                }

                String text = truncate(response, 2000);
                channel.sendMessage(text).queue(
                        msg -> log.debug("Sent Discord reply in channel {}: {} chars", channelId, text.length()),
                        err -> log.error("Failed to send Discord reply: {}", err.getMessage())
                );
                sink.success();
            } catch (Exception e) {
                log.error("Error sending Discord reply: {}", e.getMessage());
                sink.error(e);
            }
        });
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    /**
     * Handle incoming Discord messages from all three modes.
     */
    private void handleMessage(MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.getAuthor().isBot()) {
            return;
        }

        // Check allowed users
        if (!isAllowedUser(event.getAuthor().getId())) {
            log.debug("Ignoring message from unauthorized user: {}", event.getAuthor().getId());
            return;
        }

        // Check allowed servers (if configured)
        if (!isAllowedServer(event.getGuild())) {
            log.debug("Ignoring message from unauthorized server: {}",
                    event.getGuild() != null ? event.getGuild().getName() : "DM");
            return;
        }

        String content = event.getMessage().getContentRaw().trim();
        User author = event.getAuthor();
        String userId = author.getId();
        String userDisplayName = author.getName();
        String channelId = event.getChannel().getId();
        String serverId = event.getGuild() != null ? event.getGuild().getId() : null;

        // Determine the actual message content and mode
        String messageContent;
        String mode;
        Matcher mentionMatcher = MENTION_PATTERN.matcher(content);
        Matcher prefixMatcher = PREFIX_PATTERN.matcher(content);

        if (mentionMatcher.matches()) {
            // Mention-based: @BotName message
            messageContent = mentionMatcher.group(1);
            mode = "mention";
        } else if (prefixMatcher.matches()) {
            // Prefix command: !ask message
            messageContent = prefixMatcher.group(1);
            mode = "prefix";
        } else {
            // Plain text in a text channel (DM or non-command)
            messageContent = content;
            mode = "plain";
        }

        // Skip empty messages
        if (messageContent == null || messageContent.isEmpty()) {
            return;
        }

        // Create session ID per channel/server
        String sessionId = (serverId != null ? serverId + ":" : "") + channelId;

        // Get channel name if available
        String channelName = "unknown";
        try {
            channelName = event.getChannel().getName();
        } catch (Exception ignored) {
            // DM channels don't have a name property in the same way
        }

        // Create inbound message and emit to sink
        InboundMessage inbound = new InboundMessage(
                event.getMessageId(),
                "discord",
                sessionId,
                userId,
                userDisplayName,
                messageContent,
                Map.of(
                        "mode", mode,
                        "serverId", serverId != null ? serverId : "dm",
                        "channelName", channelName,
                        "channelIdRaw", channelId
                ),
                Instant.now()
        );

        inboundSink.tryEmitNext(inbound);
    }

    /**
     * Register slash commands with Discord.
     */
    private void registerSlashCommands(JDA jda) {
        // /ask command for prompting the agent
        var askCommand = Commands.slash("ask", "Ask the AI agent a question")
                .addOption(OptionType.STRING, "message", "Your question", true);

        // /help command
        var helpCommand = Commands.slash("help", "Show available commands");

        jda.updateCommands()
                .addCommands(askCommand, helpCommand)
                .queue(
                        commands -> log.info("Registered {} slash commands", commands.size()),
                        error -> log.error("Failed to register slash commands: {}", error.getMessage())
                );
    }

    private boolean isAllowedUser(String userId) {
        if (properties.getAllowedUsers() == null || properties.getAllowedUsers().isEmpty()) {
            return true; // No restriction: allow all
        }
        return properties.getAllowedUsers().contains(userId);
    }

    private boolean isAllowedServer(Guild guild) {
        if (guild == null) {
            // DMs: check if DMs are allowed (no server restriction means DMs OK)
            return properties.getAllowedServers() == null || properties.getAllowedServers().isEmpty();
        }
        if (properties.getAllowedServers() == null || properties.getAllowedServers().isEmpty()) {
            return true; // No restriction: allow all servers
        }
        return properties.getAllowedServers().contains(guild.getId());
    }

    private String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 3) + "...";
    }
}
