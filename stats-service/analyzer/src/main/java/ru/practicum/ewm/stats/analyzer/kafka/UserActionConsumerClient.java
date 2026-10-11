package ru.practicum.ewm.stats.analyzer.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
public class UserActionConsumerClient extends BaseConsumerClient<UserActionAvro> {

    public UserActionConsumerClient(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${kafka.consumer-user-actions.group-id}") String groupId,
            @Value("${kafka.consumer-user-actions.key-deserializer}") String keyDeserializer,
            @Value("${kafka.consumer-user-actions.value-deserializer}") String valueDeserializer) {
        super(bootstrapServers, groupId, keyDeserializer, valueDeserializer);
    }
}
