package com.cryptolog.wave.infrastructure.adapter.input.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeReportRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/reports/employees without JWT should return 401 Unauthorized")
    void shouldReturnUnauthorizedWhenNoJwt() throws Exception {
        mockMvc.perform(get("/api/reports/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title", is("Unauthorized")))
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("GET /api/reports/employees with JWT should return 200 OK and PDF report")
    void shouldReturnEmployeesPdfReportAtApiReportsEmployees() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/reports/employees")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"employees-report.pdf\""))
                .andReturn();

        byte[] responseBody = mvcResult.getResponse().getContentAsByteArray();
        assertThat(responseBody).isNotNull().isNotEmpty();

        // Check PDF magic header "%PDF-"
        String pdfHeader = new String(responseBody, 0, Math.min(responseBody.length, 5));
        assertThat(pdfHeader).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("GET /api/v1/reports/employees with JWT and pagination parameters should return 200 OK and PDF report")
    void shouldReturnEmployeesPdfReportWithPagination() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/reports/employees")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "id")
                        .param("sortDir", "asc")
                        .with(jwt().jwt(builder -> builder.subject("test-user").claim("preferred_username", "testuser"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"employees-report.pdf\""))
                .andReturn();

        byte[] responseBody = mvcResult.getResponse().getContentAsByteArray();
        assertThat(responseBody).isNotNull().isNotEmpty();

        // Check PDF magic header "%PDF-"
        String pdfHeader = new String(responseBody, 0, Math.min(responseBody.length, 5));
        assertThat(pdfHeader).isEqualTo("%PDF-");
    }
}
