package ru.practicum.ewm.stats.kafka;

import jakarta.annotation.PreDestroy;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaClientImpl implements KafkaClient {

    private final Producer<String, SpecificRecordBase> producer;

    public KafkaClientImpl(@Value("${kafka.bootstrap-servers}") String bootstrapServers,
                           @Value("${kafka.producer.key-serializer}") String keySerializer,
                           @Value("${kafka.producer.value-serializer}") String valueSerializer) {
        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

        this.producer = new KafkaProducer<>(config);
    }

    @Override
    public void send(String topic, String key, SpecificRecordBase message) {
        if (producer == null) {
            throw new IllegalStateException("Producer is not initialized!");
        }

        try {
            producer.send(new ProducerRecord<>(topic, key, message)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka send interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Kafka send failed", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (producer != null) {
            producer.flush();
            producer.close(Duration.ofSeconds(3));
        }
    }
}
