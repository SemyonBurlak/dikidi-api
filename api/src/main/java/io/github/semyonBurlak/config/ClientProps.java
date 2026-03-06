package io.github.semyonBurlak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.client")
public record ClientProps(WrapperProps wrapper) {
    record WrapperProps(String address, String url) {}
}
