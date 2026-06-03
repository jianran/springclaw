package com.springclaw.memory;

import com.springclaw.spring.boot.MemoryProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;

import static org.junit.jupiter.api.Assertions.*;

class ChatMemoryStoreFactoryTest {

    @Test
    void createsInMemoryByDefault() {
        MemoryProperties props = new MemoryProperties();
        props.setType("in-memory");
        props.setMaxMessages(20);

        ChatMemory memory = ChatMemoryStoreFactory.create(props);
        assertNotNull(memory);
    }
}
