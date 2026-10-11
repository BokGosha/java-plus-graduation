package ru.practicum.ewm.stats.analyzer.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_actions")
@Getter
@Setter
@IdClass(UserActionId.class)
public class UserAction {

    @Id
    private Long userId;

    @Id
    private Long eventId;

    private Double weight;

    @Column(name = "action_time")
    private Instant actionTime;
}
