package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.model.user.UserAction;
import ru.practicum.ewm.stats.analyzer.model.user.UserActionId;

import java.util.Collection;
import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, UserActionId> {

    List<UserAction> findAllByUserId(long userId);

    @Query("SELECT a FROM UserAction a WHERE a.userId = :userId ORDER BY a.actionTime DESC")
    List<UserAction> findRecentByUserId(@Param("userId") long userId, Pageable pageable);

    @Query("SELECT a.eventId, SUM(a.weight) FROM UserAction a WHERE a.eventId IN :eventIds GROUP BY a.eventId")
    List<Object[]> sumWeightsByEventIds(@Param("eventIds") Collection<Long> eventIds);
}
