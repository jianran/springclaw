package com.springclaw.mcpds.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents the result of verifying an MCP server's trust and compliance posture.
 *
 * <p>A verification result aggregates one or more {@link Attestation attestations}
 * for a given MCP server, providing a machine-verifiable snapshot of that server's
 * declared claims about its identity, security, popularity, and reliability.</p>
 *
 * <p>This record is immutable and safe for concurrent use and caching.</p>
 *
 * @author SpringClaw
 */
public record VerificationResult(String serverId, List<Attestation> attestations) {

    /**
     * Static factory for null-safe construction.
     * Requires a non-null serverId and defaults attestations to empty list if null.
     */
    public static VerificationResult of(String serverId, List<Attestation> attestations) {
        Objects.requireNonNull(serverId, "serverId must not be null");
        return new VerificationResult(
                serverId,
                attestations != null ? attestations : List.of()
        );
    }
}
