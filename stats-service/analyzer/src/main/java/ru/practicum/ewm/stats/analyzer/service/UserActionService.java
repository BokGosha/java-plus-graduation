package ru.practicum.ewm.stats.analyzer.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionService {

    void save(UserActionAvro action);
}
