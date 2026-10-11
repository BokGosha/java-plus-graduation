package ru.practicum.ewm.stats.analyzer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "user_actions")
@Getter
@Setter
@IdClass(UserId.class)
public class UserAction {

    @Id
    private Long userId;

    @Id
    private Long eventId;

    private Double score;

    private Timestamp action_time;
}
