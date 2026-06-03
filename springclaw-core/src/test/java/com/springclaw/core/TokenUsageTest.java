package com.springclaw.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenUsageTest {

    @Test
    void negativeValuesClampedToZero() {
        TokenUsage usage = new TokenUsage(-5, -10, -20);
        assertEquals(0, usage.promptTokens());
        assertEquals(0, usage.completionTokens());
        assertEquals(0, usage.totalTokens());
    }

    @Test
    void normalValuesPreserved() {
        TokenUsage usage = new TokenUsage(100, 50, 150);
        assertEquals(100, usage.promptTokens());
        assertEquals(50, usage.completionTokens());
        assertEquals(150, usage.totalTokens());
    }
}
