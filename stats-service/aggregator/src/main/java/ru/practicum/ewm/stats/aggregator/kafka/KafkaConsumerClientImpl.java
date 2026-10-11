package ru.practicum.ewm.stats.aggregator.kafka;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
public class KafkaConsumerClientImpl implements KafkaConsumerClient {

    private final Consumer<String, UserActionAvro> consumer;

    public KafkaConsumerClientImpl(@Value("${kafka.bootstrap-servers}") String bootstrapServers,
                                   @Value("${kafka.consumer.group-id}") String groupId,
                                   @Value("${kafka.consumer.key-deserializer}") String keyDeserializer,
                                   @Value("${kafka.consumer.value-deserializer}") String valueDeserializer) {
        Properties config = new Properties();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(config);
    }

    @Override
    public void subscribe(String topic) {
        consumer.subscribe(List.of(topic));
        log.info("Подписались на топик {}", topic);
    }

    @Override
    public ConsumerRecords<String, UserActionAvro> poll(Duration timeout) {
        return consumer.poll(timeout);
    }

    @Override
    public void commit() {
        consumer.commitSync();
    }

    @Override
    public void wakeup() {
        consumer.wakeup();
    }

    @PreDestroy
    public void destroy() {
        consumer.close();
    }
}
