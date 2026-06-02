package com.financialapp.finances.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String INTERNAL_TOKEN_SCHEME = "internalToken";

    @Bean
    public OpenAPI financesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finances Service API")
                        .description("Manages transactions, categories, loans, and card expenses")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(INTERNAL_TOKEN_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Internal-Token")
                                .description("Internal gateway-to-service token (X-Internal-Token header)")))
                .addSecurityItem(new SecurityRequirement().addList(INTERNAL_TOKEN_SCHEME));
    }
}
