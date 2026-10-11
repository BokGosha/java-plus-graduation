package ru.practicum.ewm.stats.aggregator.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

@Getter
@RequiredArgsConstructor
public enum ActionType {

    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    private final double weight;

    public static ActionType from(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> VIEW;
            case REGISTER -> REGISTER;
            case LIKE -> LIKE;
        };
    }
}
