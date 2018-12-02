package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaListenerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerService.class);

    @KafkaListener(topics = "Kafka_example", groupId = "testGroup")
    public void consume(String message) {
        LOGGER.info("RECEIVED message '{}' from topic 'Kafka_example'", message);
    }
}
