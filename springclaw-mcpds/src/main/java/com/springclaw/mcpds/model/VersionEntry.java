package com.springclaw.mcpds.model;

import java.time.Instant;

/**
 * Represents a version entry in the MCP Distribution Service (MCP-DS).
 *
 * <p>Holds metadata for a single published version, including the version identifier,
 * publication timestamp, and integrity checksum.</p>
 *
 * @param version    the version identifier (e.g. "1.2.3")
 * @param publishedAt the instant this version was published
 * @param checksum   the integrity checksum of the published artifact
 */
public record VersionEntry(
        String version,
        Instant publishedAt,
        String checksum
) {
}
