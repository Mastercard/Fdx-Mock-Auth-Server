package com.mastercard.fdx.mock.oauth2.server.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.Collections;

@Profile(value = {"local", "dev", "stg"})
@Configuration
@OpenAPIDefinition
public class SwaggerConfig {

	@Bean
	OpenAPI springOpenAPI() {
		Server dev = new Server().url("http://localhost:8080").description("Development Environment");
		return new OpenAPI()
				.servers(Arrays.asList(dev))
				.info(new Info().title("Welcome to FDX Mock Authorization Server!")
						.description("FDX Mock Authorization Server API reference for developer")
						.version("v1.0.0"))
				.components(new Components().addSecuritySchemes("apiKey", apiKeySecuritySchema()))
				.security(Collections.singletonList(new SecurityRequirement().addList("apiKey")));
	}

	private SecurityScheme apiKeySecuritySchema() {
		return new SecurityScheme()
				.name("Authorization")
				.description("Authorization Token to access Secured API!")
				.in(SecurityScheme.In.HEADER)
				.type(SecurityScheme.Type.APIKEY);
	}
}
