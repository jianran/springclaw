# SpringClaw

**SpringClaw** is an AI Agent Framework inspired by [OpenClaw](https://github.com/openclaw), built on top of [Spring AI](https://spring.ai/). It provides an opinionated but extensible platform for building conversational AI agents with pluggable tools, channels, and providers — all with Spring Boot's zero-boilerplate auto-configuration.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│              SpringClaw Gateway                      │
│  (HTTP/WebFlux server, channel management)           │
├─────────────────────────────────────────────────────┤
│  Channels: Web │ Slack │ Telegram │ Discord │ ...   │
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

## Quick Start

### 1. Add Dependencies

```xml
<!-- Core agent framework -->
<dependency>
    <groupId>io.springclaw</groupId>
    <artifactId>springclaw-spring-boot</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>

<!-- Gateway with REST + WebSocket API -->
<dependency>
    <groupId>io.springclaw</groupId>
    <artifactId>springclaw-gateway</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>

<!-- Built-in tools -->
<dependency>
    <groupId>io.springclaw</groupId>
    <artifactId>springclaw-tools</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>

<!-- Memory backends -->
<dependency>
    <groupId>io.springclaw</groupId>
    <artifactId>springclaw-memory</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>

<!-- Your chosen LLM provider -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

### 2. Create Your Application

```java
@SpringBootApplication
@EnableSpringClaw
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. Configure an Agent

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}

springclaw:
  agents:
    assistant:
      name: "My Assistant"
      description: "A helpful AI assistant"
      system-prompt: "You are a helpful AI assistant. Be concise and accurate."
      model:
        provider: openai
        model-id: gpt-4o
        temperature: 0.7
      tools:
        allow: ["web_search", "web_fetch", "file_read"]
      memory:
        type: in-memory
        max-messages: 50
  gateway:
    port: 8080
    web-channel-enabled: true
```

### 4. Run and Test

```bash
# Start the app
./gradlew bootRun

# Chat via REST
curl -X POST http://localhost:8080/api/agents/assistant/prompt \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, who are you?"}'

# Stream response
curl -N -X POST http://localhost:8080/api/agents/assistant/prompt/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "Tell me a joke"}'

# WebSocket (connect in browser JS)
# ws://localhost:8080/ws
```

## Defining Agents

### Via Configuration (Recommended)

```yaml
springclaw:
  agents:
    support:
      name: "Support Bot"
      system-prompt: "You are a customer support agent..."
      model:
        provider: anthropic
        model-id: claude-3-5-sonnet
```

### Via Java Beans

```java
@Bean
@Agent("tech")
public AgentConfig techAgent() {
    return new AgentConfig(
        "tech", "Tech Assistant",
        "Expert in software development",
        "You are a technical assistant...",
        new ModelConfig("openai", "gpt-4o", 0.3, null, Map.of()),
        List.of("web_search", "file_read"),
        List.of(),
        new MemoryConfig("in-memory", 100, Map.of()),
        Map.of("role", "tech"),
        null, null
    );
}
```

## Defining Tools

### With @Tool Annotation

```java
@Component
public class WeatherService {

    @Tool(description = "Get current weather for a city")
    public String getWeather(@ToolParam String city) {
        return "Sunny, 22C in " + city;
    }
}
```

### As SpringClaw ToolDefinition

```java
public class MyTool implements ToolDefinition {
    @Override
    public String getName() { return "my_tool"; }

    @Override
    public String getDescription() { return "Does something useful"; }

    @Override
    public String getSchema() { return """
        {"type":"object","properties":{"input":{"type":"string"}}}
    """; }

    @Override
    public ToolResult execute(Map<String, Object> args, ToolContext ctx) {
        return new ToolResult(null, "my_tool",
            "Result: " + args.get("input"), true, null, Map.of());
    }
}
```

## Gateway API

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/agents/{agentId}/prompt` | Send message (non-streaming) |
| POST | `/api/agents/{agentId}/prompt/stream` | Send message (SSE streaming) |
| GET | `/api/agents/{agentId}` | Get agent info |
| GET | `/api/agents` | List all agents |
| GET | `/api/agents/{agentId}/sessions/{sessionId}/messages` | Get conversation |
| DELETE | `/api/agents/{agentId}/sessions/{sessionId}` | Reset session |
| WS | `/ws` | Real-time WebSocket chat |

## Channel Adapters

| Channel | Status | Description |
|---------|--------|-------------|
| Web | Built-in | REST + WebSocket + chat widget |
| Slack | Planned | Slack bot with slash commands |
| Telegram | Planned | Telegram bot with inline queries |
| Discord | Planned | Discord bot with message commands |
| Signal | Planned | Signal bot via libsignal |

Extend with plugins:

```java
@Component
public class MyChannelPlugin implements Plugin {
    @Override
    public String getId() { return "my-channel"; }

    @Override
    public void register(PluginAPI api) {
        api.registerChannelFactory(ctx -> new MyChannelAdapter());
    }
}
```

## Plugin System

Plugins extend SpringClaw through six extension points:

| Extension | Interface | Purpose |
|-----------|-----------|---------|
| Tools | `ToolFactory` | Add new tools |
| Hooks | `Hook` | React to lifecycle events |
| Channels | `ChannelFactory` | Add messaging channels |
| Services | `Service` | Background tasks |
| Pre-processors | `PreProcessor` | Transform incoming messages |
| Post-processors | `PostProcessor` | Transform outgoing responses |

## Memory Stores

| Type | Backend | Persistence |
|------|---------|-------------|
| `in-memory` | HashMap | Ephemeral |
| `redis` | Redis | Distributed |
| `jdbc` | PostgreSQL/MySQL | Relational |
| `mongo` | MongoDB | Document |

## Module Structure

```
springclaw/
├── springclaw-core/        # Core abstractions (Agent, Tool, Hook, Plugin)
├── springclaw-spring-boot/ # Spring Boot auto-configuration
├── springclaw-gateway/     # HTTP/WebSocket server
├── springclaw-channels/    # Channel adapters (Web)
├── springclaw-tools/       # Built-in tools (search, fetch, file, cron)
├── springclaw-memory/      # ChatMemory backends
└── springclaw-samples/     # Example applications
```

## Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Submit a pull request

## License

MIT License — see [LICENSE](LICENSE) for details.
