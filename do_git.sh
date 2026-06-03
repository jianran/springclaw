#!/bin/bash
set -e
cd /Users/fatan/workspace/springclaw

echo "=== Initializing git repo ==="
git init --initial-branch=main

echo "=== Adding files ==="
git add -A

echo "=== Committing ==="
git commit -m 'Initial commit: SpringClaw AI Agent Framework

- 7-module Gradle project: core, spring-boot, gateway, channels, tools, memory, samples
- Core: Agent, Tool, Plugin, Hook, Harness, ChannelAdapter SPIs
- Spring Boot: AutoConfiguration, AgentRegistry, ToolRegistry, HookRegistry
- Gateway: WebFlux REST API + WebSocket handler
- Tools: WebSearch, WebFetch, FileRead, Cron, Session
- Memory: In-memory/Redis/JDBC/Mongo stores
- Samples: basic agent, multi-agent with routing
- Inspired by OpenClaw, built on Spring AI'

echo "=== Status ==="
git status

echo "=== Log ==="
git log --oneline -1
