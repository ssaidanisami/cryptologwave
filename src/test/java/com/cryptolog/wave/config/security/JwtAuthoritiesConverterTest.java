package com.cryptolog.wave.config.security;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthoritiesConverterTest {

    private JwtAuthoritiesConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JwtAuthoritiesConverter();
    }

    @Test
    @DisplayName("Should convert standard scopes, roles, groups and Keycloak structures into granted authorities")
    void shouldConvertAllJwtAuthorities() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("scope", "read write")
                .claim("roles", List.of("user", "admin"))
                .claim("groups", List.of("developers"))
                .claim("realm_access", Map.of("roles", List.of("keycloak-user", "default-roles-cryptolog")))
                .claim("resource_access", Map.of("cryptolog-client", Map.of("roles", List.of("api-access"))))
                .claim("authorities", List.of("CUSTOM_PERMISSION"))
                .build();

        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        List<String> authorityNames = authorities.stream().map(GrantedAuthority::getAuthority).toList();

        assertThat(authorityNames).contains(
                "SCOPE_read",
                "SCOPE_write",
                "ROLE_USER",
                "ROLE_ADMIN",
                "ROLE_DEVELOPERS",
                "ROLE_KEYCLOAK-USER",
                "ROLE_DEFAULT-ROLES-CRYPTOLOG",
                "ROLE_API-ACCESS",
                "ROLE_CRYPTOLOG-CLIENT_API-ACCESS",
                "CUSTOM_PERMISSION"
        );
    }

    @Test
    @DisplayName("Should handle empty and missing claims gracefully")
    void shouldHandleEmptyJwtGracefully() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("user-123")
                .build();

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities).isNotNull().isEmpty();
    }
}
