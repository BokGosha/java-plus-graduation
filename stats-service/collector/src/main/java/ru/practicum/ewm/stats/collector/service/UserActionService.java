package ru.practicum.ewm.stats.collector.service;

import ru.practicum.ewm.stats.collector.model.UserAction;

public interface UserActionService {

    void collect(UserAction userAction);
}
