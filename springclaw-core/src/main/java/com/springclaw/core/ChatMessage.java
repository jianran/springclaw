package com.springclaw.core;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A message in a conversation.
 *
 * @param role       message role (SYSTEM, USER, ASSISTANT, TOOL)
 * @param content    the message content
 * @param toolCallId ID of the tool call (for TOOL role messages)
 * @param toolName   name of the tool (for TOOL role messages)
 * @param metadata   additional context
 * @param timestamp  when the message was created
 */
public record ChatMessage(
    String id,
    Role role,
    String content,
    String toolCallId,
    String toolName,
    Map<String, Object> metadata,
    Instant timestamp
) {
    public ChatMessage {
        if (id == null) id = UUID.randomUUID().toString();
        if (timestamp == null) timestamp = Instant.now();
        if (metadata == null) metadata = new HashMap<>();
    }
}
