package ru.practicum.ewm.stats.aggregator.kafka;

import org.apache.avro.specific.SpecificRecordBase;

import java.time.Instant;

public interface KafkaProducerClient {

    void send(String topic, Instant timestamp, String key, SpecificRecordBase message);

    void flush();
}
