package ru.practicum.ewm.stats.analyzer.service;

import ru.practicum.ewm.stats.analyzer.model.RecommendedEvent;

import java.util.Collection;
import java.util.List;

public interface RecommendationService {

    List<RecommendedEvent> getSimilarEvents(long eventId, long userId, int maxResults);

    List<RecommendedEvent> getRecommendationsForUser(long userId, int maxResults);

    List<RecommendedEvent> getInteractionsCount(Collection<Long> eventIds);
}
