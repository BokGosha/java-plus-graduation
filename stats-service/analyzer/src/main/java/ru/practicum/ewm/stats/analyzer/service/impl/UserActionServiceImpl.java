package ru.practicum.ewm.stats.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.ActionType;
import ru.practicum.ewm.stats.analyzer.model.user.UserAction;
import ru.practicum.ewm.stats.analyzer.model.user.UserActionId;
import ru.practicum.ewm.stats.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.analyzer.service.UserActionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {

    private final UserActionRepository userActionRepository;

    @Override
    @Transactional
    public void save(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double weight = ActionType.from(action.getActionType()).getWeight();

        UserAction userAction = userActionRepository.findById(new UserActionId(userId, eventId))
                .orElseGet(() -> {
                    UserAction created = new UserAction();
                    created.setUserId(userId);
                    created.setEventId(eventId);
                    created.setWeight(0.0);
                    return created;
                });

        if (weight > userAction.getWeight()) {
            userAction.setWeight(weight);
        }

        if (userAction.getActionTime() == null || action.getTimestamp().isAfter(userAction.getActionTime())) {
            userAction.setActionTime(action.getTimestamp());
        }

        userActionRepository.save(userAction);

        log.debug("Сохранено действие: userId={}, eventId={}, weight={}", userId, eventId, userAction.getWeight());
    }
}
