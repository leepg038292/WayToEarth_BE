package com.waytoearth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Way to Earth API")
                        .description("러닝 기반 가상 여정 및 크루 시스템 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Way to Earth Team")
                                .email("team@waytoearth.com")));
    }
}