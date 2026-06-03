package com.springclaw.core;

/**
 * Types of lifecycle events that hooks can react to.
 *
 * <p>Events are categorized into session, prompt, tool, and error types.
 */
public enum EventType {

    /** A new conversation session has started. */
    SESSION_START,

    /** An existing conversation session has ended. */
    SESSION_END,

    /** A user message has been received by the agent. */
    PROMPT_RECEIVED,

    /** The agent has started generating a response. */
    RESPONSE_STARTED,

    /** The agent has completed generating a response. */
    RESPONSE_COMPLETED,

    /** A tool call is about to be executed. */
    TOOL_CALL_STARTED,

    /** A tool call completed successfully. */
    TOOL_CALL_COMPLETED,

    /** A tool call failed. */
    TOOL_CALL_FAILED,

    /** An error occurred during processing. */
    ERROR_OCCURRED,

    /** Streaming response has started. */
    STREAM_STARTED,

    /** Streaming response has completed. */
    STREAM_COMPLETED
}
