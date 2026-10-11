package ru.practicum.ewm.stats.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.collector.kafka.KafkaClient;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.collector.mapper.UserActionMapper;
import ru.practicum.ewm.stats.collector.model.UserAction;

@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {

    public static final String TOPIC = "stats.user-actions.v1";

    private final KafkaClient kafkaClient;
    private final UserActionMapper userActionMapper;

    @Override
    public void collect(UserAction userAction) {
        UserActionAvro userActionAvro = userActionMapper.mapUserAction(userAction);

        kafkaClient.send(TOPIC, userAction.getTimestamp(), userAction.getEventId(), userActionAvro);
    }
}
