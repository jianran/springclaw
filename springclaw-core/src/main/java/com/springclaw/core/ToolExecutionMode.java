package com.springclaw.core;

/**
 * How a tool should be executed relative to other tools.
 */
public enum ToolExecutionMode {
    /** Execute this tool sequentially (one at a time). */
    SEQUENTIAL,
    /** Execute this tool in parallel with other compatible tools. */
    PARALLEL
}
