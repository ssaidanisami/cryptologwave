package com.cryptolog.wave.infrastructure.adapter.input.kafka;

import com.cryptolog.wave.domain.dto.EmployeeRequest;
import com.cryptolog.wave.domain.dto.EmployeeResponse;
import com.cryptolog.wave.domain.exception.DuplicateResourceException;
import com.cryptolog.wave.domain.port.input.EmployeeUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeKafkaListenerTest {

    @Mock
    private EmployeeUseCase employeeUseCase;

    private ObjectMapper objectMapper;
    private EmployeeKafkaListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        listener = new EmployeeKafkaListener(employeeUseCase, objectMapper);
    }

    @Test
    @DisplayName("Should successfully consume and create employee from valid JSON payload")
    void shouldConsumeAndCreateEmployeeFromValidJson() throws Exception {
        EmployeeRequest request = new EmployeeRequest(
                "Alice",
                "Smith",
                "alice.smith@cryptolog.com",
                "Engineering",
                "Senior Developer",
                BigDecimal.valueOf(95000),
                LocalDate.of(2024, 3, 1)
        );
        String jsonPayload = objectMapper.writeValueAsString(request);

        EmployeeResponse mockResponse = new EmployeeResponse(
                100L,
                "Alice",
                "Smith",
                "alice.smith@cryptolog.com",
                "Engineering",
                "Senior Developer",
                BigDecimal.valueOf(95000),
                LocalDate.of(2024, 3, 1),
                null,
                null
        );
        when(employeeUseCase.createEmployee(any(EmployeeRequest.class))).thenReturn(mockResponse);

        listener.consumeEmployeeEvent(jsonPayload, "employee", 0, 10L, "emp-100");

        ArgumentCaptor<EmployeeRequest> captor = ArgumentCaptor.forClass(EmployeeRequest.class);
        verify(employeeUseCase).createEmployee(captor.capture());

        EmployeeRequest captured = captor.getValue();
        assertThat(captured.firstName()).isEqualTo("Alice");
        assertThat(captured.lastName()).isEqualTo("Smith");
        assertThat(captured.email()).isEqualTo("alice.smith@cryptolog.com");
    }

    @Test
    @DisplayName("Should handle plain text or invalid JSON gracefully without calling use case")
    void shouldHandlePlainTextGracefully() {
        listener.consumeEmployeeEvent("plain text message", "employee", 1, 15L, "key-1");

        verify(employeeUseCase, never()).createEmployee(any());
    }

    @Test
    @DisplayName("Should handle DuplicateResourceException gracefully")
    void shouldHandleDuplicateResourceExceptionGracefully() throws Exception {
        EmployeeRequest request = new EmployeeRequest(
                "Bob",
                "Jones",
                "bob.jones@cryptolog.com",
                "HR",
                "Recruiter",
                BigDecimal.valueOf(60000),
                LocalDate.now()
        );
        String jsonPayload = objectMapper.writeValueAsString(request);

        when(employeeUseCase.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new DuplicateResourceException("Employee", "email", request.email()));

        listener.consumeEmployeeEvent(jsonPayload, "employee", 2, 20L, "key-2");

        verify(employeeUseCase).createEmployee(any(EmployeeRequest.class));
    }

    @Test
    @DisplayName("Should handle unexpected runtime exception gracefully without rethrowing")
    void shouldHandleUnexpectedExceptionGracefully() throws Exception {
        EmployeeRequest request = new EmployeeRequest(
                "Charlie",
                "Brown",
                "charlie.brown@cryptolog.com",
                "Finance",
                "Analyst",
                BigDecimal.valueOf(70000),
                LocalDate.now()
        );
        String jsonPayload = objectMapper.writeValueAsString(request);

        when(employeeUseCase.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new RuntimeException("Database down"));

        listener.consumeEmployeeEvent(jsonPayload, "employee", 0, 25L, "key-3");

        verify(employeeUseCase).createEmployee(any(EmployeeRequest.class));
    }
}
