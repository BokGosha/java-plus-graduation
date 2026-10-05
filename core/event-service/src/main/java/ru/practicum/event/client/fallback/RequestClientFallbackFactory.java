package ru.practicum.event.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.event.client.RequestClient;

import java.util.Map;

@Slf4j
@Component
public class RequestClientFallbackFactory implements FallbackFactory<RequestClient> {

    @Override
    public RequestClient create(Throwable cause) {
        return (eventIds, status) -> {
            log.warn("request-service недоступен, количество заявок считаем нулевым: {}", cause.getMessage());
            return Map.of();
        };
    }
}
