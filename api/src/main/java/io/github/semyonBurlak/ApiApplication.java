package io.github.semyonburlak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiApplication {
    static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
