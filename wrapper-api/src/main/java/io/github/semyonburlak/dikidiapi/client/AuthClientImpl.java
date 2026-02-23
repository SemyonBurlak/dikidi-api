package io.github.semyonburlak.dikidiapi.client;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.semyonburlak.dikidiapi.dto.AuthResult;
import io.github.semyonburlak.dikidiapi.exception.DikidiApiException;
import io.github.semyonburlak.dikidiapi.exception.DikidiErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeType;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.github.semyonburlak.dikidiapi.client.ErrorParser.parseHttpError;

@Service
@Slf4j
public class AuthClientImpl implements AuthClient {

    private final RestClient authClient;

    private final ObjectMapper objectMapper;
    private final RateLimiter rateLimiter;


    public AuthClientImpl(RestClient authClient, ObjectMapper objectMapper, RateLimiterRegistry registry) {
        this.authClient = authClient;
        this.objectMapper = objectMapper;
        this.rateLimiter = registry.rateLimiter("dikidi");
    }

    @Override
    public AuthResult authenticate(MultiValueMap<String, String> form) {

        return rateLimiter.executeSupplier(() -> {

            ResponseEntity<JsonNode> response;
            try {
                response = authClient.post()
                        .uri("/ajax/user/auth/")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .toEntity(JsonNode.class);
            } catch (HttpClientErrorException e) {
                log.warn("Auth HTTP error: {}", e.getStatusCode().value());
                throw parseHttpError(e, objectMapper);
            }

            JsonNode body = response.getBody();
            if (body == null) {
                throw new DikidiApiException(DikidiErrorCode.EMPTY_RESPONSE, "Empty body");
            }

            JsonNode cb = body.path("callback");

            String callback;

            if (cb.getNodeType() == JsonNodeType.ARRAY
                    && !cb.isEmpty()
                    && cb.get(0).getNodeType() == JsonNodeType.STRING) {
                callback = cb.get(0).asString();
            } else {
                throw new DikidiApiException(DikidiErrorCode.UNKNOWN,
                        "Unexpected callback field: " + cb);
            }

            if (!callback.contains("sw.auth.complete")) {
                throw new DikidiApiException(DikidiErrorCode.UNKNOWN,
                        "Unexpected response: " + callback);
            }

            String sessionId = callback
                    .replace("sw.auth.complete('", "")
                    .replace("')", "");

            List<String> setCookies = response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE);

            String token = extractTokenValue(setCookies);

            String rawCookie = setCookies.stream()
                    .filter(c -> c.startsWith("token="))
                    .findFirst()
                    .orElseThrow();

            Instant expiresAt = Instant.now().plusSeconds(extractMaxAge(rawCookie));

            log.info("Successful authentication: sessionId={}", sessionId);

            return new AuthResult(sessionId, token, expiresAt);
        });
    }

    private String extractTokenValue(List<String> setCookies) {
        return setCookies.stream()
                .filter(c -> c.startsWith("token="))
                .map(c -> c.split(";")[0].trim())
                .findFirst()
                .orElseThrow(() -> new DikidiApiException(
                        DikidiErrorCode.EMPTY_RESPONSE, "No token cookie"));
    }

    private long extractMaxAge(String setCookie) {
        return Arrays.stream(setCookie.split(";"))
                .map(String::trim)
                .filter(p -> p.toLowerCase().startsWith("max-age="))
                .map(p -> p.substring("max-age=".length()))
                .mapToLong(Long::parseLong)
                .findFirst()
                .orElse(86400 * 30); // 1 month
    }
}

