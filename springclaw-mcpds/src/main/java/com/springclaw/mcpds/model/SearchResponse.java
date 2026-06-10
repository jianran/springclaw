package com.springclaw.mcpds.model;

import java.util.List;

/**
 * Response model for MCP-DS search operations.
 *
 * <p>Carries a list of search {@link SearchResult items} along with an optional
 * pagination cursor that can be used to retrieve the next page of results.</p>
 *
 * @author SpringClaw
 */
public record SearchResponse(List<SearchResult> results, String nextCursor) {

    /**
     * Static factory for null-safe construction.
     * Uses an empty list when results is null.
     */
    public static SearchResponse empty(List<SearchResult> results, String nextCursor) {
        return new SearchResponse(
                results != null ? results : List.of(),
                nextCursor
        );
    }
}
