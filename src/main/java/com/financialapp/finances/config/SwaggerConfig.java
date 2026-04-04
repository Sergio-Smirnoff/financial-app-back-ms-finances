package com.financialapp.finances.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI financesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finances Service API")
                        .description("Manages transactions, categories, loans, and card expenses")
                        .version("1.0.0"));
    }
}
