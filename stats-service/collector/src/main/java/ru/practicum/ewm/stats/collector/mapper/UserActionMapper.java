package ru.practicum.ewm.stats.collector.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.collector.model.UserAction;

@Component
public class UserActionMapper {

    public UserActionAvro mapUserAction(UserAction userAction) {
        return UserActionAvro.newBuilder()
                .setUserId(userAction.getUserId())
                .setEventId(userAction.getEventId())
                .setActionType(ActionTypeAvro.valueOf(userAction.getActionType().name()))
                .setTimestamp(userAction.getTimestamp())
                .build();
    }
}
