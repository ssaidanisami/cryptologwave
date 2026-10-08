package com.cryptolog.wave.infrastructure.adapter.input.rest;

import com.cryptolog.wave.domain.dto.EmployeeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/employees without JWT should return 401 Unauthorized")
    void shouldReturnUnauthorizedWhenNoJwt() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title", is("Unauthorized")))
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("GET /api/v1/employees with JWT should return seeded employees from Liquibase migration")
    void shouldReturnSeededEmployees() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(3)));
    }

    @Test
    @DisplayName("POST /api/v1/employees with JWT should create new employee and return 201 Created with Location header")
    void shouldCreateNewEmployee() throws Exception {
        var request = new EmployeeRequest(
                "Grace",
                "Hopper",
                "grace.hopper@example.com",
                "Engineering",
                "Distinguished Engineer",
                new BigDecimal("120000.00"),
                LocalDate.of(2024, 2, 1)
        );

        mockMvc.perform(post("/api/v1/employees")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("Grace")))
                .andExpect(jsonPath("$.lastName", is("Hopper")))
                .andExpect(jsonPath("$.email", is("grace.hopper@example.com")))
                .andExpect(jsonPath("$.salary", is(120000.00)));
    }

    @Test
    @DisplayName("POST /api/v1/employees with JWT should return 400 Bad Request when validation fails")
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        var invalidRequest = new EmployeeRequest(
                "",
                "",
                "invalid-email",
                "",
                "",
                new BigDecimal("-50.00"),
                LocalDate.now().plusDays(10) // Future date is invalid
        );

        mockMvc.perform(post("/api/v1/employees")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Invalid Request Content")))
                .andExpect(jsonPath("$.invalidFields.firstName", notNullValue()))
                .andExpect(jsonPath("$.invalidFields.email", notNullValue()))
                .andExpect(jsonPath("$.invalidFields.salary", notNullValue()))
                .andExpect(jsonPath("$.invalidFields.hireDate", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/employees with JWT should return 409 Conflict when email already exists")
    void shouldReturnConflictWhenEmailExists() throws Exception {
        var duplicateRequest = new EmployeeRequest(
                "Duplicate",
                "User",
                "alice.smith@example.com", // Already in Liquibase seed
                "Engineering",
                "Engineer",
                new BigDecimal("70000.00"),
                LocalDate.of(2023, 1, 1)
        );

        mockMvc.perform(post("/api/v1/employees")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Resource Conflict")));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} with JWT should return 200 OK when found and 404 when not found")
    void shouldHandleGetEmployeeById() throws Exception {
        // ID 1 is Alice Smith from seed data
        mockMvc.perform(get("/api/v1/employees/1")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("Alice")));

        mockMvc.perform(get("/api/v1/employees/99999")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")));
    }

    @Test
    @DisplayName("GET /api/v1/employees/by-department with JWT should filter correctly")
    void shouldFilterByDepartment() throws Exception {
        mockMvc.perform(get("/api/v1/employees/by-department")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser")))
                        .param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("GET /api/v1/employees/search with JWT should find matching employee")
    void shouldSearchEmployees() throws Exception {
        mockMvc.perform(get("/api/v1/employees/search")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser")))
                        .param("query", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName", is("Alice")));
    }

    @Test
    @DisplayName("PUT /api/v1/employees/{id} with JWT should update employee successfully")
    void shouldUpdateEmployee() throws Exception {
        var updateRequest = new EmployeeRequest(
                "Alice",
                "Smith-Updated",
                "alice.updated@example.com",
                "Engineering",
                "Principal Engineer",
                new BigDecimal("95000.00"),
                LocalDate.of(2023, 1, 15)
        );

        mockMvc.perform(put("/api/v1/employees/1")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName", is("Smith-Updated")))
                .andExpect(jsonPath("$.email", is("alice.updated@example.com")))
                .andExpect(jsonPath("$.position", is("Principal Engineer")));
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} with JWT should delete employee and return 204")
    void shouldDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/employees/1")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isNotFound());
    }
}
