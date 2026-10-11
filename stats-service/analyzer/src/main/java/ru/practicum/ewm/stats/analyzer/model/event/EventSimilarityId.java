package ru.practicum.ewm.stats.analyzer.model.event;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class EventSimilarityId implements Serializable {

    private Long eventA;

    private Long eventB;
}
