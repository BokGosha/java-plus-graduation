package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.kafka.KafkaClient;
import ru.practicum.ewm.stats.model.UserAction;

@Service
@RequiredArgsConstructor
public class UserActioNServiceImpl implements UserActionService {

    public static final String TOPIC = "stats.user-actions.v1";

    private final KafkaClient kafkaClient;
    private final UserActionMapper userActionMapper;

    @Override
    public void collect(UserAction userAction) {
        // Implementation for collecting user action
    }
}
