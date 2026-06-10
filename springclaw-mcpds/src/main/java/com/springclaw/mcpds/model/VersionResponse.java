package com.springclaw.mcpds.model;

import java.util.List;

import com.springclaw.mcpds.model.VersionEntry;

/**
 * Response carrying server identification and its supported version entries.
 *
 * @param serverId unique identifier of the MCP server
 * @param versions list of version entries advertised by the server
 */
public record VersionResponse(String serverId, List<VersionEntry> versions) {
}
