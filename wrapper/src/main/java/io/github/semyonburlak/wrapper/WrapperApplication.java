package io.github.semyonburlak.wrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WrapperApplication {

    static void main(String[] args) {
        SpringApplication.run(WrapperApplication.class, args);
    }
}
