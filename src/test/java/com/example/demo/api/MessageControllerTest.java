package com.example.demo.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.kafka.test.assertj.KafkaConditions.key;
import static org.springframework.kafka.test.assertj.KafkaConditions.value;
import static org.springframework.kafka.test.assertj.KafkaConditions.partition;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EmbeddedKafka(topics = "someTopic")
public class MessageControllerTest {

    private static final String TEMPLATE_TOPIC = "Kafka_examplw";

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, 1, TEMPLATE_TOPIC);

    @Test
    public void testTemplate() throws Exception {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testT", "false", embeddedKafka);
        DefaultKafkaConsumerFactory<Integer, String> consumerFactory = new DefaultKafkaConsumerFactory<Integer, String>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(TEMPLATE_TOPIC);
        final BlockingQueue<ConsumerRecord<Integer, String>> queue = new LinkedBlockingQueue<>();
        
        KafkaMessageListenerContainer<Integer, String> messageListenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        messageListenerContainer.setupMessageListener(new MessageListener<Integer, String>() {

            @Override
            public void onMessage(ConsumerRecord<Integer, String> record) {
                System.out.println(record);
                queue.add(record);
            }

        });
        messageListenerContainer.setBeanName("templateTests");
        messageListenerContainer.start();
        
        ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());
        Map<String, Object> senderProps = KafkaTestUtils.senderProps(embeddedKafka.getEmbeddedKafka().getBrokersAsString());
        ProducerFactory<Integer, String> producerFactory = new DefaultKafkaProducerFactory<Integer, String>(senderProps);
        KafkaTemplate<Integer, String> template = new KafkaTemplate<>(producerFactory);
        
        template.setDefaultTopic(TEMPLATE_TOPIC);
        template.sendDefault("foo");
        assertThat(queue.poll(10, TimeUnit.SECONDS)).has(value("foo"));
        
        // partition = 0, key = 2, value = "bar"
        template.sendDefault(0, 2, "bar");
        ConsumerRecord<Integer, String> received = queue.poll(10, TimeUnit.SECONDS);
        assertThat(received).has(partition(0));
        assertThat(received).has(key(2));
        assertThat(received).has(value("bar"));
        
        template.send(TEMPLATE_TOPIC, 0, 2, "baz");
        received = queue.poll(10, TimeUnit.SECONDS);
        assertThat(received).has(partition(0));
        assertThat(received).has(key(2));
        assertThat(received).has(value("baz"));
    }
}
