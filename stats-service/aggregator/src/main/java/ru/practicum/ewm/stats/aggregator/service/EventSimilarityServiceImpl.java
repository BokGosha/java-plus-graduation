package ru.practicum.ewm.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.aggregator.model.ActionType;
import ru.practicum.ewm.stats.aggregator.state.EventSimilarityState;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {

    private final EventSimilarityState state;

    @Override
    public List<EventSimilarityAvro> calculate(UserActionAvro action) {
        long eventId = action.getEventId();
        long userId = action.getUserId();

        double weight = ActionType.from(action.getActionType()).getWeight();
        double oldWeight = state.getWeight(eventId, userId);

        if (weight <= oldWeight) {
            log.debug("Вес действия не вырос: userId={}, eventId={}, old={}, new={}",
                    userId, eventId, oldWeight, weight);
            return List.of();
        }

        state.putWeight(eventId, userId, weight);
        state.addToEventSum(eventId, weight - oldWeight);

        updateMinSums(eventId, userId, weight, oldWeight);

        return collectSimilarities(eventId, userId, action.getTimestamp());
    }

    private void updateMinSums(long eventId, long userId, double weight, double oldWeight) {
        for (Map.Entry<Long, Double> userEvent : state.getUserEvents(userId).entrySet()) {
            long otherId = userEvent.getKey();

            if (otherId == eventId) {
                continue;
            }

            double otherWeight = userEvent.getValue();
            double delta = Math.min(weight, otherWeight) - Math.min(oldWeight, otherWeight);

            if (delta != 0) {
                state.addToMinSum(eventId, otherId, delta);
            }
        }
    }

    private List<EventSimilarityAvro> collectSimilarities(long eventId, long userId, Instant timestamp) {
        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (long otherId : state.getUserEvents(userId).keySet()) {
            if (otherId == eventId) {
                continue;
            }

            similarities.add(toAvro(eventId, otherId, timestamp));
        }

        similarities.sort(Comparator
                .comparingLong(EventSimilarityAvro::getEventA)
                .thenComparingLong(EventSimilarityAvro::getEventB));

        return similarities;
    }

    private EventSimilarityAvro toAvro(long first, long second, Instant timestamp) {
        long eventA = Math.min(first, second);
        long eventB = Math.max(first, second);

        return EventSimilarityAvro.newBuilder()
                .setEventA(eventA)
                .setEventB(eventB)
                .setScore(similarity(eventA, eventB))
                .setTimestamp(timestamp)
                .build();
    }

    private double similarity(long eventA, long eventB) {
        double sumA = state.getEventSum(eventA);
        double sumB = state.getEventSum(eventB);

        if (sumA == 0 || sumB == 0) {
            return 0.0;
        }

        return state.getMinSum(eventA, eventB) / (Math.sqrt(sumA) * Math.sqrt(sumB));
    }
}
