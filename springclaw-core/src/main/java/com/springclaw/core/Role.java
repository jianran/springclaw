package com.springclaw.core;

/**
 * Role of a message participant in a conversation.
 */
public enum Role {
    /** System-generated instruction or configuration message. */
    SYSTEM,
    /** Message from the user. */
    USER,
    /** Message from the AI assistant. */
    ASSISTANT,
    /** Response from a tool execution. */
    TOOL
}
