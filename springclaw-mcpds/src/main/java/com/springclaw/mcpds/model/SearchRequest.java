package com.springclaw.mcpds.model;

/**
 * Represents a search request for the MCP-DS (Model Context Protocol Data Source) API.
 *
 * <p>Defines the parameters used to query a data source, including domain filtering,
 * action type, authentication credentials, version and trust thresholds, result limits,
 * and pagination cursors.</p>
 */
public record SearchRequest(

        /**
         * The target domain to search within.
         */
        String domain,

        /**
         * The action type to filter results by.
         */
        String action,

        /**
         * Authentication credentials or token for the request.
         */
        String auth,

        /**
         * The minimum version requirement (inclusive).
         */
        String versionMin,

        /**
         * The minimum trust score threshold (inclusive).
         */
        Double trustMin,

        /**
         * Maximum number of results to return.
         */
        Integer limit,

        /**
         * Pagination cursor for fetching the next page of results.
         */
        String cursor
) {
    /**
     * Creates a new search request builder-style factory.
     *
     * @param action   the action type to filter by
     * @param trustMin minimum trust score threshold
     * @param limit    maximum results to return
     * @return a new {@code SearchRequest} instance
     */
    public static SearchRequest of(String action, Double trustMin, Integer limit) {
        return new SearchRequest(null, action, null, null, trustMin, limit, null);
    }
}
