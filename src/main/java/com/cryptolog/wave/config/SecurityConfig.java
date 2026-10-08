package com.cryptolog.wave.config;

import com.cryptolog.wave.config.security.JwtAuthoritiesConverter;
import com.cryptolog.wave.config.security.ProblemDetailAccessDeniedHandler;
import com.cryptolog.wave.config.security.ProblemDetailAuthenticationEntryPoint;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthoritiesConverter jwtAuthoritiesConverter;
    private final ProblemDetailAuthenticationEntryPoint authenticationEntryPoint;
    private final ProblemDetailAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthoritiesConverter jwtAuthoritiesConverter,
            ProblemDetailAuthenticationEntryPoint authenticationEntryPoint,
            ProblemDetailAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtAuthoritiesConverter = jwtAuthoritiesConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public OpenAPI / Swagger Documentation
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Public error & health endpoints
                        .requestMatchers(
                                "/error",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        // Protected API endpoints
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:${OIDC_JWK_SET_URI:}}") String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:${OIDC_ISSUER_URI:https://auth.cryptologwave.com/realms/cryptolog}}") String issuerUri
    ) {
        if (StringUtils.hasText(jwkSetUri)) {
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
        if (StringUtils.hasText(issuerUri)) {
            return new LazyJwtDecoder(() -> JwtDecoders.fromIssuerLocation(issuerUri));
        }
        throw new IllegalStateException(
                "Neither 'spring.security.oauth2.resourceserver.jwt.jwk-set-uri' nor " +
                "'spring.security.oauth2.resourceserver.jwt.issuer-uri' is configured."
        );
    }

    static class LazyJwtDecoder implements JwtDecoder {
        private final Supplier<JwtDecoder> delegateSupplier;
        private volatile JwtDecoder delegate;

        LazyJwtDecoder(Supplier<JwtDecoder> delegateSupplier) {
            this.delegateSupplier = delegateSupplier;
        }

        @Override
        public Jwt decode(String token) throws JwtException {
            if (this.delegate == null) {
                synchronized (this) {
                    if (this.delegate == null) {
                        try {
                            this.delegate = this.delegateSupplier.get();
                        } catch (Exception ex) {
                            throw new JwtException("Failed to initialize JwtDecoder from issuer location: " + ex.getMessage(), ex);
                        }
                    }
                }
            }
            return this.delegate.decode(token);
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtAuthoritiesConverter);
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
