package io.github.semyonburlak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.client")
public record ClientProps(WrapperProps wrapper) {
    record WrapperProps(String url, String port) {
    }
}
