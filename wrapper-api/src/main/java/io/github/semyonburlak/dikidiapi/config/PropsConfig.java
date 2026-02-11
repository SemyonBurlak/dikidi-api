package io.github.semyonburlak.dikidiapi.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ClientProps.class)
public class PropsConfig {
}
