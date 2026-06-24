package com.adriano.risk_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.Operation;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI riskApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Risk Assessment API")
                        .description("API for customer risk assessment and scoring")
                        .version("1.0"));
    }


}
