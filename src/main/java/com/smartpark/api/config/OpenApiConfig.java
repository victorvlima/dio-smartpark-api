package com.smartpark.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartPark API")
                        .description("API para gerenciamento de estacionamento de veículos.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Victor Vasconcelos Lima")
                                .email("seu.email@example.com")
                                .url("https://seusite.com"))
                        .license(new License()
                                .name("MIT license")
                                .url("https://github.com/victorvlima/dio-smartpark-api#MIT-1-ov-file")));
    }
}