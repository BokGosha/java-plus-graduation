package ru.practicum.ewm.stats.analyzer.kafka;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
public abstract class BaseConsumerClient<K, T extends SpecificRecordBase> {

    private final Consumer<K, T> consumer;

    protected BaseConsumerClient(String bootstrapServers,
                                 String groupId,
                                 String keyDeserializer,
                                 String valueDeserializer) {
        Properties config = new Properties();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(config);
    }

    public void subscribe(String topic) {
        consumer.subscribe(List.of(topic));
        log.info("Подписались на топик {}", topic);
    }

    public ConsumerRecords<K, T> poll(Duration timeout) {
        return consumer.poll(timeout);
    }

    public void commit() {
        consumer.commitSync();
    }

    public void wakeup() {
        consumer.wakeup();
    }

    @PreDestroy
    public void destroy() {
        consumer.close();
        log.info("Консьюмер закрыт");
    }
}
