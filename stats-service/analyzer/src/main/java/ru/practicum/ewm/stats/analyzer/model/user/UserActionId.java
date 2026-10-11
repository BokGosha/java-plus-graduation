package ru.practicum.ewm.stats.analyzer.model.user;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class UserActionId implements Serializable {

    private Long userId;

    private Long eventId;
}
