package com.springclaw.mcpds.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the trust score of an MCP server computed from multiple quality dimensions.
 *
 * <p>The overall trust score is a weighted average of four sub-scores:
 * namespace reputation (35%), security posture (30%), community popularity (20%),
 * and uptime reliability (15%). All sub-scores range from 0.0 to 1.0.</p>
 */
public final class TrustScore {

    private static final double NAMESPACE_WEIGHT = 0.35;
    private static final double SECURITY_WEIGHT = 0.30;
    private static final double POPULARITY_WEIGHT = 0.20;
    private static final double UPTIME_WEIGHT = 0.15;

    private final String serverId;
    private final Double score;
    private final Double namespaceScore;
    private final Double securityScore;
    private final Double popularityScore;
    private final Double uptimeScore;
    private final Instant updatedAt;

    /**
     * Creates a new TrustScore instance.
     *
     * @param serverId       unique identifier of the MCP server
     * @param score          overall trust score (0.0 - 1.0)
     * @param namespaceScore namespace reputation score (0.0 - 1.0)
     * @param securityScore  security posture score (0.0 - 1.0)
     * @param popularityScore community popularity score (0.0 - 1.0)
     * @param uptimeScore    uptime reliability score (0.0 - 1.0)
     * @param updatedAt      timestamp when this score was computed
     */
    public TrustScore(String serverId,
                      Double score,
                      Double namespaceScore,
                      Double securityScore,
                      Double popularityScore,
                      Double uptimeScore,
                      Instant updatedAt) {
        this.serverId = Objects.requireNonNull(serverId, "serverId must not be null");
        this.score = clamp(score);
        this.namespaceScore = clamp(namespaceScore);
        this.securityScore = clamp(securityScore);
        this.popularityScore = clamp(popularityScore);
        this.uptimeScore = clamp(uptimeScore);
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * Calculates the overall trust score from its four component dimensions.
     *
     * @param namespace   namespace reputation score (0.0 - 1.0)
     * @param security    security posture score (0.0 - 1.0)
     * @param popularity  community popularity score (0.0 - 1.0)
     * @param uptime      uptime reliability score (0.0 - 1.0)
     * @return a new TrustScore instance with the computed weighted average
     * @throws IllegalArgumentException if any score is null or outside [0.0, 1.0]
     */
    public static TrustScore calculate(Double namespace,
                                       Double security,
                                       Double popularity,
                                       Double uptime) {
        Double n = requireInRange(namespace, "namespace");
        Double s = requireInRange(security, "security");
        Double p = requireInRange(popularity, "popularity");
        Double u = requireInRange(uptime, "uptime");

        double overall = n * NAMESPACE_WEIGHT
                + s * SECURITY_WEIGHT
                + p * POPULARITY_WEIGHT
                + u * UPTIME_WEIGHT;

        return new TrustScore(
                null,
                overall,
                n, s, p, u,
                Instant.now()
        );
    }

    public String getServerId() {
        return serverId;
    }

    public Double getScore() {
        return score;
    }

    public Double getNamespaceScore() {
        return namespaceScore;
    }

    public Double getSecurityScore() {
        return securityScore;
    }

    public Double getPopularityScore() {
        return popularityScore;
    }

    public Double getUptimeScore() {
        return uptimeScore;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrustScore that = (TrustScore) o;
        return Double.compare(that.score, score) == 0
                && Double.compare(that.namespaceScore, namespaceScore) == 0
                && Double.compare(that.securityScore, securityScore) == 0
                && Double.compare(that.popularityScore, popularityScore) == 0
                && Double.compare(that.uptimeScore, uptimeScore) == 0
                && Objects.equals(serverId, that.serverId)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, score, namespaceScore, securityScore, popularityScore, uptimeScore, updatedAt);
    }

    @Override
    public String toString() {
        return "TrustScore{"
                + "serverId='" + serverId + '\''
                + ", score=" + score
                + ", namespace=" + namespaceScore
                + ", security=" + securityScore
                + ", popularity=" + popularityScore
                + ", uptime=" + uptimeScore
                + ", updatedAt=" + updatedAt
                + '}';
    }

    private static Double requireInRange(Double value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be between 0.0 and 1.0, got: " + value);
        }
        return value;
    }

    private static Double clamp(Double value) {
        if (value == null) return null;
        return Math.max(0.0, Math.min(1.0, value));
    }
}
