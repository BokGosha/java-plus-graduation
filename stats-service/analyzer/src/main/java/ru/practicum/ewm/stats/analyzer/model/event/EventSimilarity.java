package ru.practicum.ewm.stats.analyzer.model.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "event_similarities")
@Getter
@Setter
@IdClass(EventSimilarityId.class)
public class EventSimilarity {

    @Id
    private Long eventA;

    @Id
    private Long eventB;

    private Double score;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
