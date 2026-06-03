package com.springclaw.core;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An inbound message from a channel adapter.
 *
 * @param id                unique message identifier
 * @param channelId         the channel that received the message
 * @param sessionId         the conversation session
 * @param userId            the user who sent the message
 * @param userDisplayName   display name of the user
 * @param content           the message content
 * @param metadata          additional context (file attachments, reactions, etc.)
 * @param receivedAt        when the message was received
 */
public record InboundMessage(
    String id,
    String channelId,
    String sessionId,
    String userId,
    String userDisplayName,
    String content,
    Map<String, Object> metadata,
    Instant receivedAt
) {
    public InboundMessage {
        if (id == null) id = UUID.randomUUID().toString();
        if (receivedAt == null) receivedAt = Instant.now();
        if (metadata == null) metadata = new HashMap<>();
    }
}
