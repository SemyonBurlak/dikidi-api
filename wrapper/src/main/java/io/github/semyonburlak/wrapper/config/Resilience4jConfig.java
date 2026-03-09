package io.github.semyonburlak.wrapper.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(
            @Value("${resilience4j.ratelimiter.instances.dikidi.limit-for-period}") int limitForPeriod,
            @Value("${resilience4j.ratelimiter.instances.dikidi.limit-refresh-period}") Duration limitRefreshPeriod,
            @Value("${resilience4j.ratelimiter.instances.dikidi.timeout-duration}") Duration timeoutDuration
    ) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(limitRefreshPeriod)
                .timeoutDuration(timeoutDuration)
                .build();
        return RateLimiterRegistry.of(config);
    }

    @Bean
    public RetryRegistry retryRegistry(
            @Value("${resilience4j.retry.instances.dikidi.max-attempts}") int maxAttempts,
            @Value("${resilience4j.retry.instances.dikidi.wait-duration}") Duration waitDuration
    ) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(waitDuration)
                .build();
        return RetryRegistry.of(config);
    }
}
