package com.cryptolog.wave.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConfigTest {

    @Test
    @DisplayName("employeeTopic should create a topic named 'employee' with 3 partitions and 1 replica")
    void shouldCreateEmployeeTopicWithThreePartitions() {
        KafkaProperties properties = new KafkaProperties();
        KafkaConfig config = new KafkaConfig(properties);
        ReflectionTestUtils.setField(config, "employeeTopicName", "employee");
        ReflectionTestUtils.setField(config, "employeeTopicPartitions", 3);
        ReflectionTestUtils.setField(config, "employeeTopicReplicas", (short) 1);

        NewTopic topic = config.employeeTopic();

        assertThat(topic).isNotNull();
        assertThat(topic.name()).isEqualTo("employee");
        assertThat(topic.numPartitions()).isEqualTo(3);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("KafkaConfig should configure consumer factory and listener container factory")
    void shouldConfigureConsumerAndListenerContainerFactory() {
        KafkaProperties properties = new KafkaProperties();
        properties.setBootstrapServers(java.util.List.of("localhost:29092"));
        KafkaConfig config = new KafkaConfig(properties);

        ConsumerFactory<Object, Object> consumerFactory = config.consumerFactory();
        assertThat(consumerFactory).isNotNull();

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                config.kafkaListenerContainerFactory(null, consumerFactory);

        assertThat(factory).isNotNull();
        assertThat(factory.getConsumerFactory()).isEqualTo(consumerFactory);
    }

    @Test
    @DisplayName("KafkaConfig should configure producer factory and kafka template")
    void shouldConfigureProducerFactoryAndKafkaTemplate() {
        KafkaProperties properties = new KafkaProperties();
        properties.setBootstrapServers(java.util.List.of("localhost:29092"));
        KafkaConfig config = new KafkaConfig(properties);

        ProducerFactory<String, Object> producerFactory = config.producerFactory();
        assertThat(producerFactory).isNotNull();

        KafkaTemplate<String, Object> template = config.kafkaTemplate(producerFactory);
        assertThat(template).isNotNull();
        assertThat(template.getProducerFactory()).isEqualTo(producerFactory);
    }
}
