package io.github.semyonburlak.wrapper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dikidi.client")
public record ClientProps(String baseUrl, String authUrl, int connectTimeout, int readTimeout) {
}
