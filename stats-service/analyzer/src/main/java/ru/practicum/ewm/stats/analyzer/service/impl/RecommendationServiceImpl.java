package ru.practicum.ewm.stats.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.RecommendedEvent;
import ru.practicum.ewm.stats.analyzer.model.event.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.model.user.UserAction;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.analyzer.service.RecommendationService;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Value("${analyzer.recommendations.neighbours-count:5}")
    private int neighboursCount;

    @Override
    public List<RecommendedEvent> getSimilarEvents(long eventId, long userId, int maxResults) {
        Set<Long> interacted = getInteractedEventIds(userId);

        return eventSimilarityRepository.findAllByEvent(eventId).stream()
                .map(similarity -> toRecommended(similarity, eventId))
                .filter(recommended -> !interacted.contains(recommended.eventId()))
                .sorted(Comparator.comparingDouble(RecommendedEvent::score).reversed())
                .limit(maxResults)
                .toList();
    }

    @Override
    public List<RecommendedEvent> getRecommendationsForUser(long userId, int maxResults) {
        List<UserAction> recentActions =
                userActionRepository.findRecentByUserId(userId, PageRequest.of(0, maxResults));

        if (recentActions.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> userWeights = userActionRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight));

        List<Long> recentEventIds = recentActions.stream()
                .map(UserAction::getEventId)
                .toList();

        List<Long> candidates = eventSimilarityRepository.findAllByEvents(recentEventIds).stream()
                .flatMap(similarity -> recentEventIds.stream()
                        .filter(recentId -> isPairOf(similarity, recentId))
                        .map(recentId -> toRecommended(similarity, recentId)))
                .filter(recommended -> !userWeights.containsKey(recommended.eventId()))
                .collect(Collectors.toMap(RecommendedEvent::eventId, RecommendedEvent::score, Math::max))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .toList();

        return candidates.stream()
                .map(candidate -> new RecommendedEvent(candidate, predictScore(candidate, userWeights)))
                .sorted(Comparator.comparingDouble(RecommendedEvent::score).reversed())
                .toList();
    }

    @Override
    public List<RecommendedEvent> getInteractionsCount(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        return userActionRepository.sumWeightsByEventIds(eventIds).stream()
                .map(row -> new RecommendedEvent((Long) row[0], (Double) row[1]))
                .toList();
    }

    private double predictScore(long eventId, Map<Long, Double> userWeights) {
        List<RecommendedEvent> neighbours = eventSimilarityRepository.findAllByEvent(eventId).stream()
                .map(similarity -> toRecommended(similarity, eventId))
                .filter(recommended -> userWeights.containsKey(recommended.eventId()))
                .sorted(Comparator.comparingDouble(RecommendedEvent::score).reversed())
                .limit(neighboursCount)
                .toList();

        double weightedSum = 0.0;
        double similaritySum = 0.0;

        for (RecommendedEvent neighbour : neighbours) {
            weightedSum += userWeights.get(neighbour.eventId()) * neighbour.score();
            similaritySum += neighbour.score();
        }

        return similaritySum == 0 ? 0.0 : weightedSum / similaritySum;
    }

    private Set<Long> getInteractedEventIds(long userId) {
        return userActionRepository.findAllByUserId(userId).stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());
    }

    private boolean isPairOf(EventSimilarity similarity, long eventId) {
        return similarity.getEventA() == eventId || similarity.getEventB() == eventId;
    }

    private RecommendedEvent toRecommended(EventSimilarity similarity, long eventId) {
        long otherId = similarity.getEventA() == eventId ? similarity.getEventB() : similarity.getEventA();

        return new RecommendedEvent(otherId, similarity.getScore());
    }
}
