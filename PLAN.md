# SpringClaw — AI Agent Framework Design Plan

## Project Description

**SpringClaw** is a Java AI Agent Framework inspired by OpenClaw's plugin-driven, multi-channel architecture, built on top of Spring AI. It provides an opinionated but extensible platform for building conversational AI agents with pluggable tools, channels, and providers.

## Name: `springclaw`

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                    SpringClaw Gateway                │
│  (HTTP/WebSocket server, channel management)         │
├─────────────────────────────────────────────────────┤
│  Channels: Slack │ Telegram │ Discord │ Web │ ...   │
├─────────────────────────────────────────────────────┤
│              Agent Runtime Engine                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ Harness  │  │  Hooks   │  │ Tool Router      │  │
│  │ (spi)    │  │ (events) │  │ (policy-based)   │  │
│  └────┬─────┘  └────┬─────┘  └────────┬─────────┘  │
│       └──────────────┴─────────────────┘            │
│                     ▼                                │
│            Spring AI ChatClient                      │
├─────────────────────────────────────────────────────┤
│  Providers: OpenAI │ Anthropic │ Ollama │ Gemini ...│
└─────────────────────────────────────────────────────┘
```

---

## Module Structure

```
springclaw/
├── pom.xml                          # Parent POM (BOM)
├── springclaw-core/                 # Core abstractions (no Spring AI dep)
├── springclaw-spring-boot/          # Spring Boot auto-config
├── springclaw-gateway/              # HTTP/WebSocket server
├── springclaw-channels/             # Channel adapters
├── springclaw-channels-web/         # Built-in web channel (chat widget)
├── springclaw-tools/                # Built-in tool implementations
├── springclaw-memory/               # ChatMemory stores
├── springclaw-samples/              # Example applications
└── docs/
```

---

## Core Module (`springclaw-core`)

**No Spring Framework dependency** — pure Java abstractions.

### Key Interfaces

```java
// Agent — the top-level abstraction
public interface Agent {
    String getId();
    AgentConfig getConfig();
    AgentState getState();
    PromptResult prompt(String message, SessionContext context);
    PromptResult prompt(Prompt request);
    void registerTool(ToolDefinition tool);
    void unregisterTool(String name);
    Flux<String> streamPrompt(String message, SessionContext context);
}

// AgentConfig — configuration
public record AgentConfig(
    String id,
    String name,
    String description,
    String systemPrompt,
    ModelConfig model,
    List<String> tools,          // tool allowlist
    List<String> denylistedTools, // tool denylist
    MemoryConfig memory,
    List<HookRegistration> hooks,
    Map<String, Object> metadata
) {}

// AgentState — runtime state
public interface AgentState {
    boolean isRunning();
    List<ChatMessage> getTranscript();
    TokenUsage getUsage();
    String getCurrentSessionId();
}

// Tool — agent tools
public interface ToolDefinition {
    String getName();
    String getDescription();
    Object getSchema();          // JSON Schema for parameters
    ToolResult execute(Map<String, Object> args, ToolContext context);
}

public record ToolResult(String content, Object metadata) {}
public record ToolContext(String sessionId, String userId, Map<String, Object> extras) {}

// Hooks — lifecycle events
public interface Hook {
    String getName();
    void onEvent(HookEvent event);
}

public record HookEvent(
    String type,              // before_prompt, after_response, before_tool, after_tool, session_start, session_end
    String agentId,
    String sessionId,
    Object payload            // flexible, type depends on event
) {}

// Plugin — extension system
public interface Plugin {
    String getId();
    void register(PluginAPI api);
}

public interface PluginAPI {
    void registerToolFactory(ToolFactory factory);
    void registerHook(Hook hook);
    void registerChannelFactory(ChannelFactory factory);
    void registerGatewayHandler(GatewayHandler handler);
    void registerService(Service service);
}
```

### AgentHarness — pluggable execution backends

```java
// Like OpenClaw's harness pattern: abstract HOW the agent runs
public interface AgentHarness {
    String getId();
    boolean supports(HarnessContext context);
    AgentRunResult run(AgentRunRequest request);
    default void dispose() {}
}
```

---

## Spring Boot Module (`springclaw-spring-boot`)

**Spring Boot auto-configuration** — the "magic" that ties everything together.

### Auto-Configuration

```java
@Configuration
@ConditionalOnClass(ChatClient.class)
@EnableConfigurationProperties(SpringClawProperties.class)
public class SpringClawAutoConfiguration {

    @Bean
    public AgentRegistry agentRegistry(List<AgentDefinition> agents) { ... }

    @Bean
    public ToolRegistry toolRegistry(List<ToolCallbackProvider> providers) { ... }

    @Bean
    public HookRegistry hookRegistry(List<Hook> hooks) { ... }

    @Bean
    public AgentFactory agentFactory(...) { ... }

    @Bean
    public PluginLoader pluginLoader(...) { ... }
}

// User-facing annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableSpringClaw {}

// User-facing annotation for defining agents
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Agent {
    String value();
}
```

### Properties

```yaml
springclaw:
  agents:
    assistant:
      name: "My Assistant"
      description: "A helpful AI assistant"
      system-prompt: "You are a helpful AI assistant."
      model:
        provider: openai
        model-id: gpt-4o
        temperature: 0.7
      tools:
        allow: ["web-search", "file-read", "*"]
        deny: ["file-write", "shell-exec"]
      memory:
        type: in-memory     # or redis, postgres
        max-messages: 50
  gateway:
    port: 8080
    bind: localhost
    web-channel-enabled: true
  hooks:
    enabled: true
```

### AgentRegistry — manages all agents

```java
public interface AgentRegistry {
    Agent getAgent(String id);
    Agent getAgentOrThrow(String id);
    Collection<Agent> getAllAgents();
    Agent registerAgent(AgentConfig config);
    void unregisterAgent(String id);
}
```

### ToolRegistry — manages tools with policy

```java
public interface ToolRegistry {
    List<ToolDefinition> getEffectiveTools(String agentId);
    void registerTool(String agentId, ToolDefinition tool);
    void registerTool(ToolDefinition tool);
    void setPolicy(ToolPolicy policy);
}
```

---

## Gateway Module (`springclaw-gateway`)

Spring WebFlux-based HTTP/WebSocket server.

### GatewayServer

```java
// Long-lived server managing channels and routing
public interface GatewayServer {
    void start();
    Mono<Void> stop();
    ChannelRegistry getChannelRegistry();
}

// Routes inbound messages to agents
public interface ChannelRouter {
    Mono<String> routeInbound(InboundMessage message);
}
```

### Channel Registry

```java
public interface ChannelRegistry {
    void registerChannel(String id, ChannelAdapter adapter);
    void unregisterChannel(String id);
    Collection<ChannelAdapter> getAllChannels();
}
```

---

## Channel Module (`springclaw-channels`)

Channel adapter interface and base implementations.

```java
// Channel adapter — like OpenClaw's ChannelPlugin
public interface ChannelAdapter {
    String getId();
    ChannelMeta getMeta();
    void initialize(ChannelContext context);
    void start();
    void stop();
    boolean isRunning();
}

// Inbound message from channel
public record InboundMessage(
    String channelId,
    String sessionId,
    String userId,
    String userDisplayName,
    String content,
    Map<String, Object> metadata,
    Instant receivedAt
) {}

// Outbound delivery
public record OutboundMessage(
    String channelId,
    String sessionId,
    String userId,
    String content,
    Map<String, Object> metadata
) {}
```

### Built-in Channels
- `WebChannelAdapter` — built-in REST API + chat widget
- (Future: Slack, Telegram, Discord adapters as separate modules)

---

## Tools Module (`springclaw-tools`)

Built-in tool implementations:

- `WebSearchTool` — web search integration
- `WebFetchTool` — fetch URLs and extract content
- `FileReadTool` — read files from workspace
- `FileWriteTool` — write files to workspace
- `CodeExecutorTool` — execute code snippets (sandboxed)
- `ImageGenTool` — image generation via provider
- `CronTool` — schedule recurring tasks
- `SessionTool` — manage sessions (list, history, compact)

---

## Memory Module (`springclaw-memory`)

ChatMemory store implementations:

- `InMemoryChatMemoryStore` — ephemeral, fast
- `RedisChatMemoryStore` — distributed, persistent
- `JdbcChatMemoryStore` — relational DB (PostgreSQL, MySQL)
- `MongoChatMemoryStore` — document store

---

## Sample Applications (`springclaw-samples`)

- `sample-basic` — minimal agent with web channel
- `sample-multi-agent` — multiple agents with routing
- `sample-custom-tools` — custom tool implementation
- `sample-redis-memory` — Redis-backed conversations

---

## Key Design Decisions

1. **Spring AI is the engine, SpringClaw is the chassis**
   - ChatModel, ChatClient, ToolCallback all come from Spring AI
   - SpringClaw adds: agent lifecycle, plugin system, hooks, channels, gateway

2. **Plugin SPI via Spring beans, not ServiceLoader**
   - Leverages Spring's dependency injection
   - Plugins are just `@Component` classes that call `PluginAPI` methods

3. **Hooks replace Spring AI Advisors for agent-level concerns**
   - Spring AI Advisors = request-level (RAG, etc.)
   - SpringClaw Hooks = agent lifecycle (session start, tool call, etc.)

4. **Tool policy system**
   - Allowlist + denylist per agent
   - Glob patterns for flexible filtering
   - Evaluated at runtime via ToolRegistry

5. **Reactive-first (WebFlux)**
   - All I/O is non-blocking
   - Streaming responses via `Flux<String>`
   - Channels can be sync or async adapters

6. **Session = conversation**
   - Uses Spring AI ChatMemory under the hood
   - Sessions keyed by `channel:chatType:userId` (like OpenClaw's sessionKey)
   - Configurable memory backends

---

## Gradual Extensibility

```
Level 1: Config-only agent
  - Define agents in YAML, register tools as @Beans

Level 2: Custom agent bean
  - Implement Agent interface, register as Spring bean

Level 3: Plugin
  - Implement Plugin interface, register via PluginAPI

Level 4: Custom channel
  - Implement ChannelAdapter, register with GatewayServer

Level 5: Custom harness
  - Implement AgentHarness, swap out the agent runtime
```
