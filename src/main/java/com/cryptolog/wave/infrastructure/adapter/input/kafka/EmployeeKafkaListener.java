package com.cryptolog.wave.infrastructure.adapter.input.kafka;

import com.cryptolog.wave.domain.dto.EmployeeRequest;
import com.cryptolog.wave.domain.dto.EmployeeResponse;
import com.cryptolog.wave.domain.exception.DuplicateResourceException;
import com.cryptolog.wave.domain.port.input.EmployeeUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmployeeKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeKafkaListener.class);

    private final EmployeeUseCase employeeUseCase;
    private final ObjectMapper objectMapper;

    public EmployeeKafkaListener(EmployeeUseCase employeeUseCase, ObjectMapper objectMapper) {
        this.employeeUseCase = employeeUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.employee.name:employee}",
            groupId = "${spring.kafka.consumer.group-id:cryptolog-wave-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEmployeeEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        log.info("Received Kafka message on topic '{}' [partition={}, offset={}, key={}]: {}",
                topic, partition, offset, key, message);

        try {
            EmployeeRequest employeeRequest = objectMapper.readValue(message, EmployeeRequest.class);
            if (employeeRequest != null && employeeRequest.firstName() != null && employeeRequest.lastName() != null) {
                log.info("Processing employee creation from Kafka event for: {} {}",
                        employeeRequest.firstName(), employeeRequest.lastName());
                EmployeeResponse response = employeeUseCase.createEmployee(employeeRequest);
                log.info("Successfully created employee with ID {} from Kafka event", response.id());
            }
        } catch (JsonProcessingException ex) {
            log.info("Kafka message is not an EmployeeRequest JSON payload, processed as text message: {}", message);
        } catch (DuplicateResourceException ex) {
            log.warn("Employee from Kafka event already exists: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error processing employee Kafka message: {}", ex.getMessage(), ex);
        }
    }
}
