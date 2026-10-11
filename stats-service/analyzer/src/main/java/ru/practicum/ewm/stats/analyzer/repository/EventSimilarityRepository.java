package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.model.event.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.model.event.EventSimilarityId;

import java.util.Collection;
import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarityId> {

    @Query("SELECT s FROM EventSimilarity s WHERE s.eventA = :eventId OR s.eventB = :eventId")
    List<EventSimilarity> findAllByEvent(@Param("eventId") long eventId);

    @Query("SELECT s FROM EventSimilarity s WHERE s.eventA IN :eventIds OR s.eventB IN :eventIds")
    List<EventSimilarity> findAllByEvents(@Param("eventIds") Collection<Long> eventIds);
}
