package com.springclaw.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {

    @Test
    void createsWithDefaults() {
        ChatMessage msg = new ChatMessage(null, Role.USER, "Hello", null, null, null, null);
        assertNotNull(msg.id());
        assertNotNull(msg.timestamp());
        assertEquals(Role.USER, msg.role());
        assertEquals("Hello", msg.content());
    }

    @Test
    void toolMessageHasToolInfo() {
        ChatMessage msg = new ChatMessage(null, Role.TOOL, "result", "call-123", "web-search", null, null);
        assertEquals(Role.TOOL, msg.role());
        assertEquals("call-123", msg.toolCallId());
        assertEquals("web-search", msg.toolName());
    }
}
