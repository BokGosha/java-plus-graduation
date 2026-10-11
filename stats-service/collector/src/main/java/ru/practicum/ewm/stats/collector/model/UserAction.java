package ru.practicum.ewm.stats.collector.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserAction {

    private Long userId;
    private Long eventId;
    private ActionType actionType;
    private Instant timestamp;
}
