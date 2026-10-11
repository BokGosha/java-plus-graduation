package ru.practicum.ewm.stats.aggregator.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;

public interface KafkaConsumerClient {

    void subscribe(String topic);

    ConsumerRecords<Long, UserActionAvro> poll(Duration timeout);

    void commit();

    void wakeup();
}
