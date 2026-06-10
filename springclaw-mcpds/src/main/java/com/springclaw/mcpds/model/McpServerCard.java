package com.springclaw.mcpds.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a server card in the MCP-DS (Model Context Protocol Discovery Service).
 *
 * <p>A server card captures the essential metadata needed to discover, validate,
 * and connect to an MCP server instance, including its identity, advertised
 * capabilities, supported authentication mechanisms, available transports,
 * registry locations, and integrity checksum.</p>
 */
public record McpServerCard(
        /**
         * The namespace grouping this server (e.g. organization or project identifier).
         */
        String namespace,

        /**
         * The unique name of the server within its namespace.
         */
        String name,

        /**
         * The semantic version of the server (e.g. "1.2.3").
         */
        String version,

        /**
         * Key-value pairs describing advertised capabilities (e.g. tools, resources, prompts).
         */
        Map<String, String> capabilities,

        /**
         * Supported authentication types (e.g. "api_key", "oauth2", "none").
         */
        List<String> authTypes,

        /**
         * Supported transport protocols (e.g. "sse", "stdio", "websocket").
         */
        List<String> transports,

        /**
         * URLs of registries where this server card is published.
         */
        List<String> registryUrls,

        /**
         * Integrity checksum (e.g. SHA-256 hex digest) for card verification.
         */
        String checksum
) {
}
