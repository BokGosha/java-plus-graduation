package ru.practicum.ewm.stats.aggregator.state;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventSimilarityState {

    private final Map<Long, Map<Long, Double>> weightsByEvent = new HashMap<>();

    private final Map<Long, Map<Long, Double>> weightsByUser = new HashMap<>();

    private final Map<Long, Double> eventWeightSums = new HashMap<>();

    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public double getWeight(long eventId, long userId) {
        return weightsByEvent
                .getOrDefault(eventId, Map.of())
                .getOrDefault(userId, 0.0);
    }

    public void putWeight(long eventId, long userId, double weight) {
        weightsByEvent.computeIfAbsent(eventId, id -> new HashMap<>()).put(userId, weight);
        weightsByUser.computeIfAbsent(userId, id -> new HashMap<>()).put(eventId, weight);
    }

    public Map<Long, Double> getUserEvents(long userId) {
        return Map.copyOf(weightsByUser.getOrDefault(userId, Map.of()));
    }

    public double getEventSum(long eventId) {
        return eventWeightSums.getOrDefault(eventId, 0.0);
    }

    public void addToEventSum(long eventId, double delta) {
        eventWeightSums.merge(eventId, delta, Double::sum);
    }

    public double getMinSum(long eventA, long eventB) {
        return minWeightsSums
                .getOrDefault(Math.min(eventA, eventB), Map.of())
                .getOrDefault(Math.max(eventA, eventB), 0.0);
    }

    public void addToMinSum(long eventA, long eventB, double delta) {
        minWeightsSums
                .computeIfAbsent(Math.min(eventA, eventB), id -> new HashMap<>())
                .merge(Math.max(eventA, eventB), delta, Double::sum);
    }
}
