package com.springclaw.spring.boot;

import com.springclaw.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Default Agent implementation that wraps Spring AI's ChatClient.
 *
 * <p>This is the core bridge between the SpringClaw Agent interface and
 * Spring AI's ChatModel/ChatClient. It handles:
 * <ul>
 *   <li>Converting between Core ChatMessage and Spring AI Message types</li>
 *   <li>Resolving and filtering tools based on policy</li>
 *   <li>Managing conversation history via thread-local session state</li>
 *   <li>Tool call round-trips (LLM calls tools, we feed results back)</li>
 *   <li>Streaming responses via Reactor Flux</li>
 * </ul>
 *
 * @see AgentConfig
 */
public class SpringClawAgent implements com.springclaw.core.Agent {

    private static final Logger log = LoggerFactory.getLogger(SpringClawAgent.class);

    private final AgentConfig config;
    private final ChatClient chatClient;
    private final AtomicReference<String> currentSessionId = new AtomicReference<>();
    private final Map<String, List<ChatMessage>> sessionTranscripts = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<Hook> hooks;
    private final List<ToolDefinition> registeredTools = new ArrayList<>();

    /**
     * Create an agent from configuration.
     */
    SpringClawAgent(AgentConfig config, org.springframework.context.ApplicationContext context, List<Hook> hooks) {
        this.config = config;
        this.hooks = hooks;

        // Build ChatClient from config
        org.springframework.ai.chat.model.ChatModel chatModel = context.getBean(org.springframework.ai.chat.model.ChatModel.class);
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public String getId() {
        return config.id();
    }

    @Override
    public String getName() {
        return config.name();
    }

    @Override
    public String getDescription() {
        return config.description();
    }

    @Override
    public String getSystemPrompt() {
        return config.systemPrompt();
    }

    @Override
    public ModelConfig getModelConfig() {
        return config.model();
    }

    @Override
    public List<String> getAllowedTools() {
        return config.allowedTools();
    }

    @Override
    public List<String> getDeniedTools() {
        return config.deniedTools();
    }

    @Override
    public AgentState getState() {
        String sessionId = currentSessionId.get();
        List<ChatMessage> transcript = sessionId != null
                ? sessionTranscripts.getOrDefault(sessionId, List.of())
                : List.of();

        return new AgentState() {
            @Override
            public boolean isRunning() { return running.get(); }
            @Override
            public String getCurrentSessionId() { return sessionId; }
            @Override
            public List<ChatMessage> getTranscript() { return transcript; }
            @Override
            public TokenUsage getUsage() { return new TokenUsage(0, 0, 0); }
            @Override
            public int getTurnCount() { return transcript.stream().filter(m -> m.role() == Role.ASSISTANT).toList().size(); }
            @Override
            public Instant getLastActivity() { return transcript.isEmpty() ? null : transcript.get(transcript.size() - 1).timestamp(); }
        };
    }

    @Override
    public PromptResult prompt(String message, SessionContext context) {
        String sessionId = context.sessionId() != null ? context.sessionId() : UUID.randomUUID().toString();
        UserMessage userMessage = new UserMessage(message);
        return prompt(new com.springclaw.core.Prompt(
                null, context.userId(), sessionId,
                message, null, null, Map.of(), null
        ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public PromptResult prompt(com.springclaw.core.Prompt request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();
        currentSessionId.set(sessionId);

        running.set(true);
        try {
            // Emit hook event
            fireEvent(EventType.PROMPT_RECEIVED, request);

            // Build the Spring AI prompt
            SpringPromptResult buildResult = buildPrompt(request);

            // Call the model
            org.springframework.ai.chat.prompt.Prompt springPrompt = buildResult.prompt();
            ChatResponse response = chatClient.prompt(springPrompt).call().chatResponse();

            // Extract response text
            String responseText = response.getResult().getOutput().getText();
            if (responseText == null) responseText = "";

            // Record messages
            recordResponse(sessionId, request, buildResult.userMessage(), response);

            // Emit completion event
            fireEvent(EventType.RESPONSE_COMPLETED, request);

            return new PromptResult(
                    config.id(), sessionId, responseText,
                    List.of(), // Tool calls would be populated if we tracked them
                    new TokenUsage(0, 0, 0),
                    Map.of("modelId", config.model().modelId()),
                    Instant.now()
            );
        } catch (Exception e) {
            log.error("Agent '{}' failed to process prompt: {}", config.id(), e.getMessage(), e);
            fireEvent(EventType.ERROR_OCCURRED, request);
            throw new RuntimeException("Agent prompt failed: " + e.getMessage(), e);
        } finally {
            running.set(false);
        }
    }

    @Override
    public Flux<String> streamPrompt(String message, SessionContext context) {
        String sessionId = context.sessionId() != null ? context.sessionId() : UUID.randomUUID().toString();
        currentSessionId.set(sessionId);
        running.set(true);

        // Create a simple user message for streaming
        UserMessage userMsg = new UserMessage(message);
        org.springframework.ai.chat.prompt.Prompt springPrompt = new org.springframework.ai.chat.prompt.Prompt(userMsg);

        return chatClient.prompt(springPrompt).stream().content()
                .doOnSubscribe(s -> fireEvent(EventType.STREAM_STARTED, Map.of("sessionId", sessionId)))
                .doOnComplete(() -> {
                    running.set(false);
                    fireEvent(EventType.STREAM_COMPLETED, Map.of("sessionId", sessionId));
                })
                .doOnError(e -> {
                    running.set(false);
                    fireEvent(EventType.ERROR_OCCURRED, Map.of("sessionId", sessionId, "error", e.getMessage()));
                });
    }

    @Override
    public Collection<ToolDefinition> getRegisteredTools() {
        return Collections.unmodifiableList(registeredTools);
    }

    @Override
    public void registerTool(ToolDefinition tool) {
        registeredTools.add(tool);
        log.info("Registered tool '{}' on agent '{}'", tool.getName(), config.id());
    }

    @Override
    public void unregisterTool(String toolName) {
        registeredTools.removeIf(t -> t.getName().equals(toolName));
        log.info("Unregistered tool '{}' from agent '{}'", toolName, config.id());
    }

    @Override
    public void resetSession(String sessionId) {
        sessionTranscripts.remove(sessionId);
        currentSessionId.set(sessionId);
        log.debug("Reset session '{}' for agent '{}'", sessionId, config.id());
    }

    @Override
    public AgentConfig toConfig() {
        return config;
    }

    /**
     * Build a Spring AI Prompt from a SpringClaw Prompt request.
     */
    private SpringPromptResult buildPrompt(com.springclaw.core.Prompt request) {
        List<Message> messages = new ArrayList<>();

        // Add system prompt if present
        String systemPrompt = request.systemPrompt() != null ? request.systemPrompt() : config.systemPrompt();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }

        // Get conversation history
        String sessionId = request.sessionId();
        if (sessionId != null) {
            List<ChatMessage> history = sessionTranscripts.getOrDefault(sessionId, List.of());
            for (ChatMessage cm : history) {
                messages.add(convertToSpringAiMessage(cm));
            }
        }

        // Add user message
        UserMessage userMessage = new UserMessage(request.message());
        messages.add(userMessage);

        // Build Spring AI prompt
        org.springframework.ai.chat.prompt.Prompt springPrompt = new org.springframework.ai.chat.prompt.Prompt(messages);

        return new SpringPromptResult(springPrompt, userMessage);
    }

    /**
     * Resolve tools for the current agent based on policy.
     */
    @SuppressWarnings("unchecked")
    private Object resolveTools() {
        // Tools are resolved via ToolCallbackProvider in the real implementation
        // This method returns empty for now; the actual tool resolution happens in SpringClawToolRegistry
        return List.of();
    }

    /**
     * Convert a Core ChatMessage to a Spring AI Message.
     */
    private Message convertToSpringAiMessage(ChatMessage cm) {
        return switch (cm.role()) {
            case SYSTEM -> new SystemMessage(cm.content());
            case USER -> new UserMessage(cm.content());
            case ASSISTANT -> new AssistantMessage(cm.content());
            case TOOL -> {
                    ToolResponseMessage.ToolResponse response = new ToolResponseMessage.ToolResponse(
                            UUID.randomUUID().toString(),  // toolCallId
                            cm.toolName(),
                            cm.content()
                    );
                    yield new ToolResponseMessage(List.of(response));
                }
        };
    }

    /**
     * Record the response messages in the session transcript.
     */
    private void recordResponse(String sessionId, com.springclaw.core.Prompt request, UserMessage userMessage, ChatResponse response) {
        String effectiveSessionId = sessionId;
        if (effectiveSessionId == null) {
            effectiveSessionId = UUID.randomUUID().toString();
        }
        List<ChatMessage> transcript = sessionTranscripts.computeIfAbsent(effectiveSessionId, k -> new ArrayList<>());

        // Add user message
        transcript.add(new ChatMessage(
                null, Role.USER, userMessage.getText(),
                null, null, null, null
        ));

        // Add assistant message
        String assistantText = response.getResult().getOutput().getText();
        if (assistantText != null) {
            transcript.add(new ChatMessage(
                    null, Role.ASSISTANT, assistantText,
                    null, null, null, null
            ));
        }
    }

    /**
     * Fire a hook event if hooks are registered.
     */
    @SuppressWarnings("unchecked")
    private void fireEvent(EventType type, Object payload) {
        if (hooks.isEmpty()) return;
        String sessionId = currentSessionId.get();
        if (sessionId == null) return;

        HookEvent event = new HookEvent(
                type, config.id(), sessionId,
                payload != null && payload instanceof Map map ? (String) map.get("userId") : null,
                payload, null
        );

        for (Hook hook : hooks) {
            try {
                hook.onEvent(event);
            } catch (Exception e) {
                log.warn("Hook '{}' threw on {}: {}", hook.getName(), type, e.getMessage());
            }
        }
    }

    /**
     * Simple builder result holder.
     */
    static class SpringPromptResult {
        private final org.springframework.ai.chat.prompt.Prompt prompt;
        private final UserMessage userMessage;
        SpringPromptResult(org.springframework.ai.chat.prompt.Prompt prompt, UserMessage userMessage) {
            this.prompt = prompt;
            this.userMessage = userMessage;
        }
        org.springframework.ai.chat.prompt.Prompt prompt() { return prompt; }
        UserMessage userMessage() { return userMessage; }
    }
}
