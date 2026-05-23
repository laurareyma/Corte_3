package com.sabana.riesgo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    /**
     * RestTemplate con timeouts HTTP explícitos (complementa Resilience4j TimeLimiter).
     * Connect/read acotados evitan hilos bloqueados indefinidamente si el peer no responde.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
            @Value("${http.client.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${http.client.read-timeout-ms}") int readTimeoutMs) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
