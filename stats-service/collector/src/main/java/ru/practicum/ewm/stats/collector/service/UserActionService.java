package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.stats.model.UserAction;

public interface UserActionService {

    void collect(UserAction userAction);
}
