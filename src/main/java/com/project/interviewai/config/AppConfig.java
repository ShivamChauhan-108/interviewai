package com.project.interviewai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for application-wide beans.
 * Registers a RestTemplate bean so it can be injected into services
 * like GeminiAiService for making external HTTP calls.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
