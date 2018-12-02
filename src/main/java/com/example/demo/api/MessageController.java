package com.example.demo.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;

    public MessageController(final KafkaTemplate<String, String> kafkaTemplate, @Value("${kafka.topic.name}") final String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @GetMapping("/kafka/{message}")
    public String getMessage(@PathVariable(name = "message") final String message) {
        LOGGER.info("Sending message '{}' to topic '{}'", message, topicName);
        kafkaTemplate.send(topicName, message);
        return message;
    }
}
