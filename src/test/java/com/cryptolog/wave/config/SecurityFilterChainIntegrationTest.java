package com.cryptolog.wave.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterChainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Swagger OpenAPI docs should be publicly accessible without authentication")
    void shouldAllowPublicAccessToSwaggerDocs() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected API endpoints must reject unauthenticated requests with RFC 7807 problem details")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title", is("Unauthorized")))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.type", is("https://cryptologwave.com/errors/unauthorized")))
                .andExpect(jsonPath("$.instance", is("/api/v1/employees")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Protected API endpoints must allow access when valid JWT bearer token is present")
    void shouldAllowAuthenticatedRequestWithJwt() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .with(jwt().jwt(builder -> builder
                                .subject("employee-admin")
                                .claim("preferred_username", "alice")
                                .claim("roles", "ADMIN"))))
                .andExpect(status().isOk());
    }
}
