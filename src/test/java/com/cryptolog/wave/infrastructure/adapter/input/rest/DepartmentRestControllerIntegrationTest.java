package com.cryptolog.wave.infrastructure.adapter.input.rest;

import com.cryptolog.wave.domain.dto.DepartmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class DepartmentRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/departments without JWT should return 401 Unauthorized")
    void shouldReturnUnauthorizedWhenNoJwt() throws Exception {
        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title", is("Unauthorized")))
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("GET /api/v1/departments with JWT should return seeded departments")
    void shouldReturnSeededDepartments() throws Exception {
        mockMvc.perform(get("/api/v1/departments")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    @DisplayName("POST /api/v1/departments with JWT should create department successfully")
    void shouldCreateDepartment() throws Exception {
        var request = new DepartmentRequest("Legal", "Legal Department");

        mockMvc.perform(post("/api/v1/departments")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Legal")))
                .andExpect(jsonPath("$.description", is("Legal Department")));
    }

    @Test
    @DisplayName("DELETE /api/v1/departments/{id} should fail when employees are assigned to it")
    void shouldFailDeletingDepartmentWhenEmployeesAssigned() throws Exception {
        // Engineering department (id 1) has employees from seed data
        mockMvc.perform(delete("/api/v1/departments/1")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Illegal Argument")))
                .andExpect(jsonPath("$.detail", is("Department 'Engineering' cannot be deleted because employees are still assigned to it")));
    }

    @Test
    @DisplayName("DELETE /api/v1/departments/{id} should succeed when no employees are assigned")
    void shouldDeleteDepartmentWhenNoEmployeesAssigned() throws Exception {
        // Create an empty department
        var request = new DepartmentRequest("Temporary", "Temporary Department");
        String responseContent = mockMvc.perform(post("/api/v1/departments")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number id = com.jayway.jsonpath.JsonPath.read(responseContent, "$.id");

        mockMvc.perform(delete("/api/v1/departments/" + id)
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/departments/" + id)
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isNotFound());
    }
}
