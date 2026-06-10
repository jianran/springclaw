package com.springclaw.mcpds.model;

import java.time.Instant;

/**
 * Represents an MCP-DS (Model Context Protocol - Determination &amp; Safety) attestation.
 *
 * <p>An attestation binds a JWS-signed assertion to its issuing and auditing parties,
 * along with validity windows and a classification type. It serves as machine-verifiable
 * proof of compliance, capability, or operational status claims made by an MCP server
 * or model provider.</p>
 *
 * <p>The {@code type} field distinguishes between the following attestation categories:</p>
 * <ul>
 *   <li>{@code namespace} — declares the registered namespace or identity of a service</li>
 *   <li>{@code security}   — attests to security posture, certifications, or audit results</li>
 *   <li>{@code popularity} — conveys usage metrics, adoption signals, or community trust</li>
 *   <li>{@code uptime}     — confirms service availability and reliability commitments</li>
 * </ul>
 *
 * <p>This record is immutable and therefore safe for concurrent use and for inclusion
 * in cached or distributed data structures.</p>
 *
 * @param issuer     the entity that issued this attestation (e.g. a CA, org, or trust anchor)
 * @param auditor    the independent party that verified or co-signed the attestation
 * @param issuedAt   the instant at which this attestation became valid
 * @param expiresAt  the instant at which this attestation ceases to be valid
 * @param jws        the JWS (JSON Web Signature) payload carrying the signed attestation claim
 * @param type       the attestation category: {@code namespace}, {@code security},
 *                   {@code popularity}, or {@code uptime}
 */
public record Attestation(
        String issuer,
        String auditor,
        Instant issuedAt,
        Instant expiresAt,
        String jws,
        Type type
) {

    /**
     * Categories of MCP-DS attestation.
     */
    public enum Type {

        /** Declares registered namespace or service identity. */
        NAMESPACE,

        /** Security posture, certifications, or audit results. */
        SECURITY,

        /** Usage metrics, adoption signals, or community trust. */
        POPULARITY,

        /** Service availability and reliability commitments. */
        UPTIME
    }

}
