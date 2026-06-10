package com.springclaw.mcpds;

import com.springclaw.mcpds.model.Attestation;
import com.springclaw.mcpds.model.TrustScore;
import com.springclaw.mcpds.model.VerificationResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes a trust score for an MCP server from its JWS-verified attestations.
 *
 * <p>The evaluator groups attestations by their {@link Attestation.Type}
 * (namespace, security, popularity, uptime), derives a per-type sub-score in the
 * {@code [0.0, 1.0]} range, and combines them with fixed weights defined in
 * {@link TrustScore#calculate(Double, Double, Double, Double)}:</p>
 *
 * <ul>
 *   <li>namespace  — 35%</li>
 *   <li>security   — 30%</li>
 *   <li>popularity — 20%</li>
 *   <li>uptime     — 15%</li>
 * </ul>
 *
 * @author SpringClaw
 */
public final class TrustEvaluator {

    /** Minimum number of attestations per type to reach full sub-score. */
    private static final int FULL_TRUST_THRESHOLD = 3;

    /** Score awarded when at least one attestation of a given type is present and valid. */
    private static final double PRESENT_SCORE = 0.5;

    public TrustEvaluator() {
        // public for DI containers
    }

    /**
     * Evaluates the trustworthiness of an MCP server based on its attestation list.
     *
     * <p>For each attestation type the evaluator counts how many attestations are
     * present and not expired. The per-type sub-score is computed as:</p>
     *
     * <pre>
     *   score = PRESENT_SCORE + (1.0 - PRESENT_SCORE) * min(attestationCount / FULL_TRUST_THRESHOLD, 1.0)
     * </pre>
     *
     * <p>Types with zero valid attestations receive a sub-score of {@code 0.0}.
     * The four sub-scores are then combined using the weighted formula defined in
     * {@link TrustScore#calculate}.</p>
     *
     * @param result the verification result containing attestations; must not be {@code null}
     * @return a {@link TrustScore} reflecting the server's trust posture
     */
    public TrustScore evaluate(VerificationResult result) {
        if (result == null || result.attestations().isEmpty()) {
            return TrustScore.calculate(0.0, 0.0, 0.0, 0.0);
        }

        Instant now = Instant.now();
        Map<Attestation.Type, List<Attestation>> grouped =
                result.attestations().stream()
                        .filter(a -> a.expiresAt() == null || a.expiresAt().isAfter(now))
                        .collect(Collectors.groupingBy(Attestation::type));

        double namespaceScore = perTypeScore(grouped.get(Attestation.Type.NAMESPACE));
        double securityScore  = perTypeScore(grouped.get(Attestation.Type.SECURITY));
        double popularityScore = perTypeScore(grouped.get(Attestation.Type.POPULARITY));
        double uptimeScore    = perTypeScore(grouped.get(Attestation.Type.UPTIME));

        return TrustScore.calculate(namespaceScore, securityScore, popularityScore, uptimeScore);
    }

    /**
     * Checks whether a trust score meets or exceeds the given minimum threshold.
     *
     * @param score     the trust score to check; must not be {@code null}
     * @param minTrust  the minimum acceptable trust value in the range {@code [0.0, 1.0]}
     * @return {@code true} if {@code score.score >= minTrust}, {@code false} otherwise
     * @throws IllegalArgumentException if {@code score} or {@code minTrust} is {@code null}
     */
    public boolean isTrustworthy(TrustScore score, double minTrust) {
        if (score == null) {
            throw new IllegalArgumentException("score must not be null");
        }
        if (Double.compare(minTrust, 0.0) < 0 || Double.compare(minTrust, 1.0) > 0) {
            throw new IllegalArgumentException("minTrust must be between 0.0 and 1.0, got: " + minTrust);
        }
        double trust = score.getScore() != null ? score.getScore() : 0.0;
        return trust >= minTrust;
    }

    /**
     * Derives a per-type sub-score from the list of attestations of that type.
     *
     * @param attestations the list of valid attestations of a single type; may be {@code null} or empty
     * @return a score in {@code [0.0, 1.0]}
     */
    private double perTypeScore(List<Attestation> attestations) {
        if (attestations == null || attestations.isEmpty()) {
            return 0.0;
        }
        int count = attestations.size();
        return PRESENT_SCORE + (1.0 - PRESENT_SCORE) * Math.min((double) count / FULL_TRUST_THRESHOLD, 1.0);
    }
}
