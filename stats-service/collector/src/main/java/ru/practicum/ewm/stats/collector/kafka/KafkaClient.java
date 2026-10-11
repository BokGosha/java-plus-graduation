package ru.practicum.ewm.stats.collector.kafka;

import org.apache.avro.specific.SpecificRecordBase;

import java.time.Instant;

public interface KafkaClient {

    void send(String topic, Instant timestamp, Long key, SpecificRecordBase message);
}
