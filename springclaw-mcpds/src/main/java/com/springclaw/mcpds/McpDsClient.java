package com.springclaw.mcpds;

import java.net.URI;
import java.util.List;

import com.springclaw.mcpds.model.McpServerCard;
import com.springclaw.mcpds.model.SearchRequest;
import com.springclaw.mcpds.model.SearchResponse;
import com.springclaw.mcpds.model.VerificationResult;
import com.springclaw.mcpds.model.VersionResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Reactive client for the MCP-DS (Model Context Protocol Data Source) API.
 *
 * <p>Provides non-blocking access to MCP server discovery, versioning, and
 * verification endpoints through Spring WebFlux's {@link WebClient}. All methods
 * return {@link Mono} instances suitable for reactive composition.</p>
 *
 * <p><b>Thread safety:</b> {@code McpDsClient} instances are immutable and thread-safe
 * after construction. Reuse a single instance across your application and share it
 * with the DI container.</p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * McpDsClient client = new McpDsClient("https://mcpds.example.com/api");
 *
 * client.search(new SearchRequest("ai", null, null, null, null, 20, null))
 *       .flatMap(resp -> resp.results())
 *       .doOnNext(card -> log.info("Found: {}", card.name()))
 *       .blockLast();
 * }</pre>
 *
 * @author SpringClaw
 */
public class McpDsClient {

    private static final ParameterizedTypeReference<SearchResponse> SEARCH_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<McpServerCard> SERVER_CARD_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<VersionResponse> VERSION_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<VerificationResult> VERIFICATION_RESULT_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    /**
     * Creates a new {@code McpDsClient} pointing at the given base URL.
     *
     * @param baseUrl the base URL of the MCP-DS API (e.g. {@code "https://mcpds.example.com/api"});
     *                must not be {@code null} or blank
     * @throws IllegalArgumentException if {@code baseUrl} is {@code null} or blank
     */
    public McpDsClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be null or blank");
        }
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }

    /**
     * Returns the underlying {@link WebClient} instance.
     *
     * <p>Useful for advanced customization such as adding filters, interceptors,
     * or custom codecs. Any modifications affect all subsequent requests.</p>
     *
     * @return the configured {@code WebClient}
     */
    public WebClient webClient() {
        return webClient;
    }

    /**
     * Searches the MCP-DS catalog for servers matching the given criteria.
     *
     * <p>This method sends a POST request to the {@code /search} endpoint with the
     * supplied {@link SearchRequest} body and returns a {@link Mono} that emits
     * a {@link SearchResponse} containing matching {@link com.springclaw.mcpds.model.SearchResult}s.</p>
     *
     * <h4>HTTP Mapping</h4>
     * <pre>POST {baseUrl}/search</pre>
     *
     * @param request the search request parameters; must not be {@code null}
     * @return a {@code Mono} emitting the search response
     * @throws WebClientResponseException if the HTTP response indicates an error
     */
    public Mono<SearchResponse> search(SearchRequest request) {
        return webClient.post()
                .uri("/search")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SEARCH_RESPONSE_TYPE);
    }

    /**
     * Retrieves the server card for a named MCP server.
     *
     * <p>A server card contains identity, capabilities, transport, and integrity
     * metadata for an MCP server instance.</p>
     *
     * <h4>HTTP Mapping</h4>
     * <pre>GET {baseUrl}/servers/{namespace}/{name}/card</pre>
     *
     * @param namespace the namespace (e.g. organization or project) grouping the server;
     *                  must not be {@code null} or blank
     * @param name      the unique name of the server within its namespace;
     *                  must not be {@code null} or blank
     * @return a {@code Mono} emitting the server card
     * @throws IllegalArgumentException if {@code namespace} or {@code name} is blank
     * @throws WebClientResponseException if the HTTP response indicates an error
     */
    public Mono<McpServerCard> getServerCard(String namespace, String name) {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("namespace must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/servers/{namespace}/{name}/card")
                        .build(namespace, name))
                .retrieve()
                .bodyToMono(SERVER_CARD_TYPE);
    }

    /**
     * Retrieves the supported versions for a named MCP server.
     *
     * <p>Returns a {@link VersionResponse} carrying the server identifier and a list
     * of {@link com.springclaw.mcpds.model.VersionEntry} objects, one per supported version.</p>
     *
     * <h4>HTTP Mapping</h4>
     * <pre>GET {baseUrl}/servers/{namespace}/{name}/versions</pre>
     *
     * @param namespace the namespace grouping the server; must not be {@code null} or blank
     * @param name      the unique name of the server; must not be {@code null} or blank
     * @return a {@code Mono} emitting the version response
     * @throws IllegalArgumentException if {@code namespace} or {@code name} is blank
     * @throws WebClientResponseException if the HTTP response indicates an error
     */
    public Mono<VersionResponse> getVersions(String namespace, String name) {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("namespace must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/servers/{namespace}/{name}/versions")
                        .build(namespace, name))
                .retrieve()
                .bodyToMono(VERSION_RESPONSE_TYPE);
    }

    /**
     * Verifies the trust and compliance posture of a named MCP server.
     *
     * <p>Performs a verification lookup and returns a {@link VerificationResult}
     * containing one or more {@link com.springclaw.mcpds.model.Attestation} entries
     * that describe the server's declared claims about identity, security, popularity,
     * and reliability.</p>
     *
     * <h4>HTTP Mapping</h4>
     * <pre>GET {baseUrl}/servers/{namespace}/{name}/verify</pre>
     *
     * @param namespace the namespace grouping the server; must not be {@code null} or blank
     * @param name      the unique name of the server; must not be {@code null} or blank
     * @return a {@code Mono} emitting the verification result
     * @throws IllegalArgumentException if {@code namespace} or {@code name} is blank
     * @throws WebClientResponseException if the HTTP response indicates an error
     */
    public Mono<VerificationResult> verify(String namespace, String name) {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("namespace must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/servers/{namespace}/{name}/verify")
                        .build(namespace, name))
                .retrieve()
                .bodyToMono(VERIFICATION_RESULT_TYPE);
    }
}
