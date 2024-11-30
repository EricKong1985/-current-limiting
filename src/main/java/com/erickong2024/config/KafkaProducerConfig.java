package com.erickong2024.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;


@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        HashMap props = new HashMap();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", StringSerializer.class);
        props.put("value.serializer", StringSerializer.class);

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate().send(topic, message);
    }
}
