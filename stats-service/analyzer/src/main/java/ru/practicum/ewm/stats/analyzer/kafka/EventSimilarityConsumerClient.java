package ru.practicum.ewm.stats.analyzer.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
public class EventSimilarityConsumerClient extends BaseConsumerClient<String, EventSimilarityAvro> {

    public EventSimilarityConsumerClient(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${kafka.consumer-events-similarity.group-id}") String groupId,
            @Value("${kafka.consumer-events-similarity.key-deserializer}") String keyDeserializer,
            @Value("${kafka.consumer-events-similarity.value-deserializer}") String valueDeserializer) {
        super(bootstrapServers, groupId, keyDeserializer, valueDeserializer);
    }
}
