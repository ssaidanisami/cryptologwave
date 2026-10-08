package com.cryptolog.wave.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_BEARER = "BearerAuth";
    private static final String SECURITY_SCHEME_OIDC = "OpenIdConnect";

    @Bean
    public OpenAPI employeeManagementOpenAPI(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:https://auth.cryptologwave.com/realms/cryptolog}")
            String issuerUri
    ) {
        String oidcDiscoveryUrl = (issuerUri != null && !issuerUri.isBlank())
                ? (issuerUri.endsWith("/") ? issuerUri + ".well-known/openid-configuration" : issuerUri + "/.well-known/openid-configuration")
                : "https://auth.cryptologwave.com/realms/cryptolog/.well-known/openid-configuration";

        return new OpenAPI()
                .info(new Info()
                        .title("CryptologWave Employee Management API")
                        .description("RESTful CRUD API for Employee Management built with Spring Boot 3, Java 21, and Spring Security OAuth2 / OpenID Connect.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cryptolog Wave Team")
                                .email("contact@cryptologwave.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_BEARER)
                        .addList(SECURITY_SCHEME_OIDC))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_BEARER, new SecurityScheme()
                                .name(SECURITY_SCHEME_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide the JWT Bearer token obtained from your OpenID Connect identity provider."))
                        .addSecuritySchemes(SECURITY_SCHEME_OIDC, new SecurityScheme()
                                .name(SECURITY_SCHEME_OIDC)
                                .type(SecurityScheme.Type.OPENIDCONNECT)
                                .openIdConnectUrl(oidcDiscoveryUrl)
                                .description("OpenID Connect Discovery Endpoint")));
    }
}
