package ru.practicum.ewm.stats.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.event.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.model.event.EventSimilarityId;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.service.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {

    private final EventSimilarityRepository eventSimilarityRepository;

    @Override
    @Transactional
    public void save(EventSimilarityAvro similarity) {
        long eventA = Math.min(similarity.getEventA(), similarity.getEventB());
        long eventB = Math.max(similarity.getEventA(), similarity.getEventB());

        EventSimilarity stored = eventSimilarityRepository.findById(new EventSimilarityId(eventA, eventB))
                .orElseGet(() -> {
                    EventSimilarity created = new EventSimilarity();
                    created.setEventA(eventA);
                    created.setEventB(eventB);
                    return created;
                });

        if (stored.getUpdatedAt() != null && stored.getUpdatedAt().isAfter(similarity.getTimestamp())) {
            log.debug("Пропущено устаревшее сходство пары {} и {}", eventA, eventB);
            return;
        }

        stored.setScore(similarity.getScore());
        stored.setUpdatedAt(similarity.getTimestamp());

        eventSimilarityRepository.save(stored);

        log.debug("Сохранено сходство: eventA={}, eventB={}, score={}", eventA, eventB, similarity.getScore());
    }
}
