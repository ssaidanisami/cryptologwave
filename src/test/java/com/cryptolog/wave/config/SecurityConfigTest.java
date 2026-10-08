package com.cryptolog.wave.config;

import com.cryptolog.wave.config.security.JwtAuthoritiesConverter;
import com.cryptolog.wave.config.security.ProblemDetailAccessDeniedHandler;
import com.cryptolog.wave.config.security.ProblemDetailAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthoritiesConverter jwtAuthoritiesConverter;

    @Mock
    private ProblemDetailAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private ProblemDetailAccessDeniedHandler accessDeniedHandler;

    @Test
    @DisplayName("Should create JwtDecoder when valid jwk-set-uri is provided")
    void shouldCreateJwtDecoderFromJwkSetUri() {
        SecurityConfig config = new SecurityConfig(jwtAuthoritiesConverter, authenticationEntryPoint, accessDeniedHandler);

        JwtDecoder decoder = config.jwtDecoder("http://localhost:8080/jwks", "");
        assertThat(decoder).isNotNull();
    }

    @Test
    @DisplayName("Should create lazy JwtDecoder when issuer-uri is provided without calling discovery endpoint on startup")
    void shouldCreateLazyJwtDecoderFromIssuerUri() {
        SecurityConfig config = new SecurityConfig(jwtAuthoritiesConverter, authenticationEntryPoint, accessDeniedHandler);

        // Does not throw UnknownHostException during instantiation
        JwtDecoder decoder = config.jwtDecoder("", "https://nonexistent-auth.cryptologwave.com/realms/cryptolog");
        assertThat(decoder).isNotNull();

        // Throws JwtException on decode attempt when host is unreachable
        assertThatThrownBy(() -> decoder.decode("some.jwt.token"))
                .isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class)
                .hasMessageContaining("Failed to initialize JwtDecoder from issuer location");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when neither jwk-set-uri nor issuer-uri is configured")
    void shouldThrowExceptionWhenNoUriConfigured() {
        SecurityConfig config = new SecurityConfig(jwtAuthoritiesConverter, authenticationEntryPoint, accessDeniedHandler);

        assertThatThrownBy(() -> config.jwtDecoder("", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Neither 'spring.security.oauth2.resourceserver.jwt.jwk-set-uri' nor 'spring.security.oauth2.resourceserver.jwt.issuer-uri' is configured");
    }

    @Test
    @DisplayName("Should create JwtAuthenticationConverter configured with custom authorities converter")
    void shouldCreateJwtAuthenticationConverter() {
        SecurityConfig config = new SecurityConfig(jwtAuthoritiesConverter, authenticationEntryPoint, accessDeniedHandler);

        var authConverter = config.jwtAuthenticationConverter();
        assertThat(authConverter).isNotNull();
    }
}
