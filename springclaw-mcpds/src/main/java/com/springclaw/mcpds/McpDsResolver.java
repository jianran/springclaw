package com.springclaw.mcpds;

import com.springclaw.mcpds.model.SearchRequest;
import com.springclaw.mcpds.model.SearchResponse;
import com.springclaw.mcpds.model.SearchResult;
import reactor.core.publisher.Flux;

/**
 * Bridges MCP-DS server discovery with MCP connections.
 *
 * <p>Resolves functional requirements (actions) into a stream of MCP server URIs,
 * filtering by trust threshold. Acts as the "resolver step before connection"
 * described in the MCP-DS spec.
 *
 * <p>See <a href="https://github.com/jianran/mcp-ds">MCP-DS Spec</a>.
 */
public class McpDsResolver {

    private final McpDsClient client;
    private final TrustEvaluator trustEvaluator;
    private final double minTrust;

    /**
     * Create a resolver with the given client, evaluator, and trust threshold.
     *
     * @param client         the MCP-DS API client
     * @param trustEvaluator the trust evaluation component
     * @param minTrust       minimum acceptable trust score (0.0-1.0)
     */
    public McpDsResolver(McpDsClient client, TrustEvaluator trustEvaluator, double minTrust) {
        this.client = client;
        this.trustEvaluator = trustEvaluator;
        this.minTrust = minTrust;
    }

    /**
     * Resolve MCP server URIs matching the given action and trust threshold.
     *
     * @param action the functional capability to search for (e.g., "file_operations", "web_research")
     * @return Flux of server URIs whose trust scores meet the minimum threshold
     */
    public Flux<String> resolve(String action) {
        return resolveWithDetails(action)
                .map(SearchResult::uri);
    }

    /**
     * Resolve MCP servers matching the given action, returning full result details.
     *
     * @param action the functional capability to search for
     * @return Flux of search results (URIs, trust scores, auth types, versions, publishers)
     */
    public Flux<SearchResult> resolveWithDetails(String action) {
        return client.search(new SearchRequest(null, action, null, null, minTrust, 20, null))
                .flatMapIterable(SearchResponse::results)
                .filter(result -> result.trust() != null && result.trust() >= minTrust);
    }
}
