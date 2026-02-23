package io.github.semyonburlak.dikidiapi.client;


import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.semyonburlak.dikidiapi.dto.DikidiResponse;
import io.github.semyonburlak.dikidiapi.dto.DikidiResult;
import io.github.semyonburlak.dikidiapi.util.ResponseParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.function.Function;

@Component
public class GetClient {

    private final RestClient restClient;
    private final ResponseParser responseParser;
    private final RateLimiter rateLimiter;

    public GetClient(RestClient restClient, ResponseParser responseParser, RateLimiterRegistry registry) {
        this.restClient = restClient;
        this.responseParser = responseParser;
        this.rateLimiter = registry.rateLimiter("dikidi");
    }

    public <T> DikidiResponse<T> get(String path, Map<String, String> queryParams, Class<T> dataType) {

        return execute(client -> client
                .get()
                .uri(uri -> {
                    UriBuilder builder = uri.path(path);
                    queryParams.forEach(builder::queryParam);
                    return builder.build();
                })
                .retrieve()
                .toEntity(JsonNode.class), dataType);
    }

    public <T> DikidiResponse<T> post(String path,
                                      MultiValueMap<String, String> body,
                                      String cookies,
                                      Class<T> dataType) {
        return post(path, null, body, cookies, dataType);
    }

    public <T> DikidiResponse<T> post(
            String path,
            Map<String, String> queryParams,
            MultiValueMap<String, String> body,
            String cookies,
            Class<T> dataType
    ) {
        return execute(client -> client
                .post()
                .uri(uri -> {
                    UriBuilder builder = uri.path(path);
                    queryParams.forEach(builder::queryParam);
                    return builder.build();
                })
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    if (cookies != null && !cookies.isBlank()) {
                        headers.set(HttpHeaders.COOKIE, cookies);
                    }
                })
                .body(body)
                .retrieve()
                .toEntity(JsonNode.class), dataType);
    }

    private <T> DikidiResponse<T> execute(
            Function<RestClient, ResponseEntity<JsonNode>> request,
            Class<T> dataType
    ) {
        try {
            ResponseEntity<JsonNode> response = rateLimiter.executeSupplier(
                    () -> request.apply(restClient)
            );
            DikidiResult<T> result = responseParser.parse(response.getBody(), dataType);
            return DikidiResponse.of(result, response.getHeaders());
        } catch (RestClientResponseException e) {
            return new DikidiResponse<>(
                    DikidiResult.fail(e.getStatusCode().value(), "HTTP " + e.getStatusCode()),
                    null,
                    e.getResponseHeaders()
            );
        } catch (RequestNotPermitted e) {
            return new DikidiResponse<>(
                    DikidiResult.fail("RATE_LIMITED", "Слишком много запросов"),
                    null,
                    HttpHeaders.EMPTY
            );
        } catch (Exception e) {
            return new DikidiResponse<>(
                    DikidiResult.fail("NETWORK", e.getMessage()),
                    null,
                    HttpHeaders.EMPTY
            );
        }
    }
}
