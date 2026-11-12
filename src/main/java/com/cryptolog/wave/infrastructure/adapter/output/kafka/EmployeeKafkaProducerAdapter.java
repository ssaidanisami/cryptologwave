package com.cryptolog.wave.infrastructure.adapter.output.kafka;

import com.cryptolog.wave.domain.dto.EmployeeResponse;
import com.cryptolog.wave.domain.port.output.EmployeeEventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmployeeKafkaProducerAdapter implements EmployeeEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(EmployeeKafkaProducerAdapter.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String employeeTopic;

    public EmployeeKafkaProducerAdapter(
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.employee.name:employee}") String employeeTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.employeeTopic = employeeTopic;
    }

    @Override
    public void publishEmployeeCreated(EmployeeResponse employeeResponse) {
        publishEvent(employeeResponse, "created");
    }

    @Override
    public void publishEmployeeUpdated(EmployeeResponse employeeResponse) {
        publishEvent(employeeResponse, "updated");
    }

    private void publishEvent(EmployeeResponse employeeResponse, String eventType) {
        if (employeeResponse == null) {
            log.warn("Cannot publish null employee response to Kafka topic '{}'", employeeTopic);
            return;
        }

        try {
            String messageKey = employeeResponse.id() != null ? String.valueOf(employeeResponse.id()) : null;
            String payload = objectMapper.writeValueAsString(employeeResponse);

            log.info("Publishing employee {} event to Kafka topic '{}' with key '{}': {}",
                    eventType, employeeTopic, messageKey, payload);

            kafkaTemplate.send(employeeTopic, messageKey, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish employee {} event for employee id {}: {}",
                                    eventType, employeeResponse.id(), ex.getMessage(), ex);
                        } else if (result != null) {
                            log.info("Successfully published employee {} event to topic '{}' [partition={}, offset={}] for employee id {}",
                                    eventType,
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset(),
                                    employeeResponse.id());
                        }
                    });
        } catch (JsonProcessingException ex) {
            log.error("Error serializing employee {} event to JSON for employee id {}: {}",
                    eventType, employeeResponse.id(), ex.getMessage(), ex);
        }
    }
}
