package com.springclaw.mcpds.model;

/**
 * Represents a single search result from an MCP Data Source (MCP-DS) search operation.
 *
 * <p>Each result contains the essential metadata needed to identify, rank, and
 * authenticate a discovered MCP server endpoint:
 *
 * <ul>
 *   <li>{@code uri} — the canonical endpoint URI of the MCP server.</li>
 *   <li>{@code trust} — a numeric trust score in the range [0.0, 1.0] indicating
 *       reliability and reputation.</li>
 *   <li>{@code auth} — the required authentication method (e.g. "none", "api_key", "oauth2").</li>
 *   <li>{@code version} — the Semantic Version string of the MCP server implementation.</li>
 *   <li>{@code publisher} — the organization or individual that published this MCP server.</li>
 * </ul>
 *
 * <p>This record is immutable and suitable for use as a key in hash-based collections.
 */
public record SearchResult(
        /**
         * Canonical URI of the MCP server endpoint.
         */
        String uri,

        /**
         * Numeric trust score in the range [0.0, 1.0], where 1.0 represents the
         * highest level of trust and reliability.
         */
        Double trust,

        /**
         * Required authentication method for accessing this MCP server.
         * Examples: "none", "api_key", "oauth2", "jwt".
         */
        String auth,

        /**
         * Semantic Version string of the MCP server (e.g. "1.2.3").
         */
        String version,

        /**
         * The organization or individual that published this MCP server.
         */
        String publisher
) {

    /**
     * Creates a new {@code SearchResult} with null-safety checks.
     *
     * @param uri       canonical URI (must not be null or blank)
     * @param trust     trust score in [0.0, 1.0], or {@code null} for unknown
     * @param auth      authentication method (must not be null or blank)
     * @param version   semantic version string (may be {@code null} if unknown)
     * @param publisher publisher name (must not be null or blank)
     * @throws IllegalArgumentException if uri, auth, or publisher is null or blank
     */
    public SearchResult {
        if (uri == null || uri.isBlank()) {
            throw new IllegalArgumentException("uri must not be null or blank");
        }
        if (auth == null || auth.isBlank()) {
            throw new IllegalArgumentException("auth must not be null or blank");
        }
        if (publisher == null || publisher.isBlank()) {
            throw new IllegalArgumentException("publisher must not be null or blank");
        }
        if (trust != null && (trust < 0.0 || trust > 1.0)) {
            throw new IllegalArgumentException("trust must be in the range [0.0, 1.0]");
        }
    }
}
