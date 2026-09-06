package com.aias.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Baseline OpenAPI / Swagger metadata configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AI Appraisal & Assessment Platform API")
                .version("v0.0.1")
                .description("REST API documentation for the AI Appraisal & Assessment Platform (AIAS)")
                .contact(new Contact().name("AIAS Platform Team"))
                .license(new License().name("Proprietary")));
    }
}
