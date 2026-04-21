package com.trohub.backend.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trohub Backend API")
                        .version("0.0.1")
                        .description("API documentation for Trohub backend")
                        .contact(new Contact().name("Trohub Team").email("devs@trohub.local"))
                        .license(new License().name("MIT"))
                );
    }
}

