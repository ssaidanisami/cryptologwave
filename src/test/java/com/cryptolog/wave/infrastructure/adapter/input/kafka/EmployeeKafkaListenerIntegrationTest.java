package com.cryptolog.wave.infrastructure.adapter.input.kafka;

import com.cryptolog.wave.domain.dto.EmployeeRequest;
import com.cryptolog.wave.domain.port.output.EmployeeRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 3,
        topics = {"employee"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestPropertySource(properties = {
        "spring.kafka.listener.auto-startup=true",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "app.kafka.topics.employee.name=employee",
        "app.kafka.topics.employee.partitions=3"
})
class EmployeeKafkaListenerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmployeeRepositoryPort employeeRepositoryPort;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void waitForKafkaAssignment() {
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    @DisplayName("Should receive employee event from Kafka topic 'employee' with 3 partitions and persist employee")
    void shouldConsumeEmployeeEventFromKafkaAndPersist() throws Exception {
        String uniqueEmail = "kafka.listener.test@cryptolog.com";
        EmployeeRequest request = new EmployeeRequest(
                "Kafka",
                "ConsumerTest",
                uniqueEmail,
                "Infrastructure",
                "DevOps Engineer",
                BigDecimal.valueOf(88000),
                LocalDate.now()
        );

        String payload = objectMapper.writeValueAsString(request);

        // Send to partition 1 of the 3 partitions
        kafkaTemplate.send("employee", 1, "emp-key-1", payload).get(10, TimeUnit.SECONDS);

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    assertThat(employeeRepositoryPort.existsByEmail(uniqueEmail)).isTrue();
                });
    }

    @Test
    @DisplayName("Should receive employee update event on Kafka topic 'employee' across partitions in same consumer group")
    void shouldConsumeEmployeeUpdateEventFromKafka() throws Exception {
        String updatePayload = """
                {
                    "id": 999,
                    "firstName": "UpdatedFirstName",
                    "lastName": "UpdatedLastName",
                    "email": "updated.employee@cryptolog.com",
                    "department": "Engineering",
                    "position": "Staff Engineer",
                    "salary": 140000,
                    "hireDate": "2024-01-15"
                }
                """;

        // Send to partition 2 with employee id key
        kafkaTemplate.send("employee", 2, "999", updatePayload).get(10, TimeUnit.SECONDS);

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    assertThat(employeeRepositoryPort.existsByEmail("updated.employee@cryptolog.com")).isTrue();
                });
    }
}
