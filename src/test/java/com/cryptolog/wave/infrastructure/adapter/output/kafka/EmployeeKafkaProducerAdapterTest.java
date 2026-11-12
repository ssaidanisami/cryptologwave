package com.cryptolog.wave.infrastructure.adapter.output.kafka;

import com.cryptolog.wave.domain.dto.EmployeeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeKafkaProducerAdapterTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper objectMapper;
    private EmployeeKafkaProducerAdapter producerAdapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        producerAdapter = new EmployeeKafkaProducerAdapter(kafkaTemplate, objectMapper, "employee");
    }

    @Test
    @DisplayName("Should publish employee created event successfully to Kafka")
    void shouldPublishEmployeeCreatedSuccessfully() {
        EmployeeResponse response = new EmployeeResponse(
                42L,
                "Grace",
                "Hopper",
                "grace.hopper@cryptolog.com",
                "Computer Science",
                "Pioneer",
                BigDecimal.valueOf(120000),
                LocalDate.of(2023, 1, 1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq("employee"), eq("42"), any(String.class))).thenReturn(future);

        producerAdapter.publishEmployeeCreated(response);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("employee"), eq("42"), payloadCaptor.capture());

        String capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload).contains("grace.hopper@cryptolog.com");
        assertThat(capturedPayload).contains("Grace");
    }

    @Test
    @DisplayName("Should publish employee updated event successfully to Kafka")
    void shouldPublishEmployeeUpdatedSuccessfully() {
        EmployeeResponse response = new EmployeeResponse(
                42L,
                "Grace",
                "Hopper",
                "grace.hopper@cryptolog.com",
                "Computer Science",
                "Senior Pioneer",
                BigDecimal.valueOf(135000),
                LocalDate.of(2023, 1, 1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq("employee"), eq("42"), any(String.class))).thenReturn(future);

        producerAdapter.publishEmployeeUpdated(response);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("employee"), eq("42"), payloadCaptor.capture());

        String capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload).contains("grace.hopper@cryptolog.com");
        assertThat(capturedPayload).contains("Senior Pioneer");
    }

    @Test
    @DisplayName("Should handle null employee gracefully")
    void shouldHandleNullEmployeeGracefully() {
        producerAdapter.publishEmployeeCreated(null);
        producerAdapter.publishEmployeeUpdated(null);

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
