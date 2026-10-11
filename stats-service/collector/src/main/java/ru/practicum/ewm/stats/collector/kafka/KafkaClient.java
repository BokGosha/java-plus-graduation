package ru.practicum.ewm.stats.kafka;

import org.apache.avro.specific.SpecificRecordBase;

public interface KafkaClient {

    void send(String topic, String key, SpecificRecordBase message);
}
