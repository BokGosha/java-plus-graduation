package ru.practicum.ewm.stats.aggregator.kafka;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

@Slf4j
@Component
public class KafkaProducerClientImpl implements KafkaProducerClient {

    private final Producer<String, SpecificRecordBase> producer;

    public KafkaProducerClientImpl(@Value("${kafka.bootstrap-servers}") String bootstrapServers,
                                   @Value("${kafka.producer.key-serializer}") String keySerializer,
                                   @Value("${kafka.producer.value-serializer}") String valueSerializer) {
        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

        this.producer = new KafkaProducer<>(config);
    }

    @Override
    public void send(String topic, Instant timestamp, String key, SpecificRecordBase message) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic, null, timestamp.toEpochMilli(), key, message
        );

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Не удалось отправить сообщение в топик {}: {}", topic, exception.getMessage());
            }
        });
    }

    @Override
    public void flush() {
        producer.flush();
    }

    @PreDestroy
    public void destroy() {
        producer.flush();
        producer.close(Duration.ofSeconds(3));
    }
}
