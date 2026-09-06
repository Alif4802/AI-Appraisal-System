package com.aias.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Development and Test placeholder security configuration.
 * Active ONLY under 'dev' and 'test' profiles.
 * Not active in production.
 */
@Configuration
@EnableWebSecurity
@Profile({"dev", "test"})
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Allow OpenAPI / Swagger UI in dev/test
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Minimal actuator exposure
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // All other requests require authentication or permit in dev placeholder
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
