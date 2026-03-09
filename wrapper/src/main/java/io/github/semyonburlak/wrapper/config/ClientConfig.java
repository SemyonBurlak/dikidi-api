package io.github.semyonburlak.wrapper.config;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.semyonburlak.wrapper.client.DikidiHttpClient;
import io.github.semyonburlak.wrapper.util.ResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Configuration
@Slf4j
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
            RateLimiterRegistry rateLimiterRegistry,
            RetryRegistry retryRegistry
    ) {
        return new DikidiHttpClient(restClient, parser, rateLimiterRegistry, retryRegistry);
    }

    @Bean("authHttpClient")
    public DikidiHttpClient authDikidiClient(
            @Qualifier("authRestClient") RestClient restClient,
            ResponseParser parser,
            RateLimiterRegistry rateLimiterRegistry,
            RetryRegistry retryRegistry
    ) {
        return new DikidiHttpClient(restClient, parser, rateLimiterRegistry, retryRegistry);
    }

    private RestClient buildRestClient(SimpleClientHttpRequestFactory requestFactory, String baseUrl) {
        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper(
            @Value("${spring.mvc.format.date}") String datePattern,
            @Value("${spring.mvc.format.date-time}") String dateTimePattern
    ) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);

        SimpleModule module = new SimpleModule()
                .addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(module)
                .build();
    }
}
