package io.github.semyonburlak.dikidiapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.client")
public record ClientProps(String baseUrl, int connectTimeout, int readTimeout) {
}
