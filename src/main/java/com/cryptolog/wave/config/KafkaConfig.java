package com.cryptolog.wave.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String DEFAULT_EMPLOYEE_TOPIC = "employee";
    public static final int DEFAULT_EMPLOYEE_PARTITIONS = 3;
    public static final short DEFAULT_EMPLOYEE_REPLICAS = 1;

    private final KafkaProperties kafkaProperties;

    @Value("${app.kafka.topics.employee.name:" + DEFAULT_EMPLOYEE_TOPIC + "}")
    private String employeeTopicName;

    @Value("${app.kafka.topics.employee.partitions:" + DEFAULT_EMPLOYEE_PARTITIONS + "}")
    private int employeeTopicPartitions;

    @Value("${app.kafka.topics.employee.replicas:" + DEFAULT_EMPLOYEE_REPLICAS + "}")
    private short employeeTopicReplicas;

    @Value("${spring.kafka.listener.auto-startup:true}")
    private boolean autoStartup;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    /**
     * Defines the 'employee' Kafka topic with 3 partitions and 1 replica.
     */
    @Bean
    public NewTopic employeeTopic() {
        return TopicBuilder.name(employeeTopicName)
                .partitions(employeeTopicPartitions)
                .replicas(employeeTopicReplicas)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ConsumerFactory.class)
    public ConsumerFactory<Object, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(null));
    }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        if (configurer != null) {
            configurer.configure(factory, consumerFactory);
        } else {
            factory.setConsumerFactory(consumerFactory);
        }
        factory.setAutoStartup(autoStartup);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(ProducerFactory.class)
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(null));
    }

    @Bean
    @ConditionalOnMissingBean(KafkaTemplate.class)
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
