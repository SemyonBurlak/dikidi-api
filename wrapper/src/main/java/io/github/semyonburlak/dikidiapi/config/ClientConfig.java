package io.github.semyonburlak.dikidiapi.config;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.semyonburlak.dikidiapi.client.DikidiHttpClient;
import io.github.semyonburlak.dikidiapi.util.ResponseParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Configuration
public class ClientConfig {

    private final ClientProps clientProps;

    @Bean
    public SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(clientProps.connectTimeout());
        requestFactory.setReadTimeout(clientProps.readTimeout());
        return requestFactory;
    }

    @Bean("restClient")
    public RestClient restClient(SimpleClientHttpRequestFactory requestFactory) {
        return buildRestClient(requestFactory, clientProps.baseUrl());
    }

    @Bean("authRestClient")
    public RestClient authRestClient(SimpleClientHttpRequestFactory requestFactory) {
        return buildRestClient(requestFactory, clientProps.authUrl());
    }

    @Bean("dikidiHttpClient")
    public DikidiHttpClient dikidiClient(
            @Qualifier("restClient") RestClient restClient,
            ResponseParser parser,
            RateLimiterRegistry registry
    ) {
        return new DikidiHttpClient(restClient, parser, registry);
    }

    @Bean("authHttpClient")
    public DikidiHttpClient authDikidiClient(
            @Qualifier("authRestClient") RestClient restClient,
            ResponseParser parser,
            RateLimiterRegistry registry
    ) {
        return new DikidiHttpClient(restClient, parser, registry);
    }

    private RestClient buildRestClient(SimpleClientHttpRequestFactory requestFactory, String baseUrl) {
        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}
