package com.cryptolog.wave.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

import static org.assertj.core.api.Assertions.assertThat;

class ProfilesConfigurationTest {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
    private final Path resourcesDir = Path.of("src", "main", "resources");

    @Test
    @DisplayName("Base application.yml should define active profile fallback to dev and common properties")
    void shouldLoadBaseApplicationYml() throws IOException {
        List<PropertySource<?>> sources = loader.load(
                "application.yml",
                new FileSystemResource(resourcesDir.resolve("application.yml"))
        );
        assertThat(sources).isNotEmpty();

        PropertySource<?> source = sources.getFirst();
        assertThat(source.getProperty("spring.application.name")).isEqualTo("cryptolog-wave");
        assertThat(source.getProperty("spring.profiles.active")).isEqualTo("${SPRING_PROFILES_ACTIVE:dev}");
        assertThat(source.getProperty("spring.jpa.open-in-view")).isEqualTo(false);
        assertThat(source.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(source.getProperty("spring.liquibase.enabled")).isEqualTo(true);
    }

    @Test
    @DisplayName("application-dev.yml should configure development properties")
    void shouldLoadDevProfileYml() throws IOException {
        List<PropertySource<?>> sources = loader.load(
                "application-dev.yml",
                new FileSystemResource(resourcesDir.resolve("application-dev.yml"))
        );
        assertThat(sources).isNotEmpty();

        PropertySource<?> source = sources.getFirst();
        assertThat(source.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"))
                .isEqualTo("${OIDC_ISSUER_URI:http://localhost:8081/realms/cryptolog}");
        assertThat(source.getProperty("spring.jpa.show-sql")).isEqualTo(true);
        assertThat(source.getProperty("springdoc.swagger-ui.enabled")).isEqualTo(true);
        assertThat(source.getProperty("spring.datasource.hikari.pool-name")).isEqualTo("DevHikariPool");
    }

    @Test
    @DisplayName("application-uat.yml should configure UAT staging properties")
    void shouldLoadUatProfileYml() throws IOException {
        List<PropertySource<?>> sources = loader.load(
                "application-uat.yml",
                new FileSystemResource(resourcesDir.resolve("application-uat.yml"))
        );
        assertThat(sources).isNotEmpty();

        PropertySource<?> source = sources.getFirst();
        assertThat(source.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"))
                .isEqualTo("${OIDC_ISSUER_URI:https://auth-uat.cryptologwave.com/realms/cryptolog}");
        assertThat(source.getProperty("spring.jpa.show-sql")).isEqualTo(false);
        assertThat(source.getProperty("springdoc.swagger-ui.enabled")).isEqualTo(true);
        assertThat(source.getProperty("spring.datasource.hikari.pool-name")).isEqualTo("UATHikariPool");
        assertThat(source.getProperty("spring.datasource.hikari.maximum-pool-size")).isEqualTo(15);
    }

    @Test
    @DisplayName("application-prod.yml should configure production-hardened properties")
    void shouldLoadProdProfileYml() throws IOException {
        List<PropertySource<?>> sources = loader.load(
                "application-prod.yml",
                new FileSystemResource(resourcesDir.resolve("application-prod.yml"))
        );
        assertThat(sources).isNotEmpty();

        PropertySource<?> source = sources.getFirst();
        assertThat(source.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"))
                .isEqualTo("${OIDC_ISSUER_URI:https://auth.cryptologwave.com/realms/cryptolog}");
        assertThat(source.getProperty("spring.jpa.show-sql")).isEqualTo(false);
        assertThat(source.getProperty("springdoc.swagger-ui.enabled")).isEqualTo("${SWAGGER_ENABLED:false}");
        assertThat(source.getProperty("server.shutdown")).isEqualTo("graceful");
        assertThat(source.getProperty("spring.datasource.hikari.pool-name")).isEqualTo("ProdHikariPool");
        assertThat(source.getProperty("spring.datasource.hikari.maximum-pool-size")).isEqualTo(30);
    }
}
