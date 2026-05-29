package com.mahmoud.maalflow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;


@Configuration@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "MaalFlow API",
                version = "1.0",
                description = "MaalFlow Backend APIs"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("MaalFlow API")
//                        .version("1.0")
//                        .description("MaalFlow Backend APIs"));
//    }
}
