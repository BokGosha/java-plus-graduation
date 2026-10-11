package ru.practicum.ewm.stats.collector.mapper;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.collector.model.ActionType;
import ru.practicum.ewm.stats.collector.model.UserAction;
import ru.practicum.ewm.stats.proto.UserActionProto;

import ru.practicum.ewm.stats.proto.ActionTypeProto;

import java.time.Instant;

@Component
public class UserActionProtoMapper {

    public UserAction mapUserActionProto(UserActionProto userActionProto) {
        return UserAction.builder()
                .userId(userActionProto.getUserId())
                .eventId(userActionProto.getEventId())
                .actionType(mapActionType(userActionProto.getActionType()))
                .timestamp(mapTimestamp(userActionProto.getTimestamp()))
                .build();
    }

    private ActionType mapActionType(ActionTypeProto actionType) {
        return switch (actionType) {
            case ACTION_VIEW -> ActionType.VIEW;
            case ACTION_REGISTER -> ActionType.REGISTER;
            case ACTION_LIKE -> ActionType.LIKE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown action type: " + actionType);
        };
    }

    private Instant mapTimestamp(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
