package ru.practicum.event.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.event.client.CommentClient;

import java.util.Map;

@Slf4j
@Component
public class CommentClientFallbackFactory implements FallbackFactory<CommentClient> {

    @Override
    public CommentClient create(Throwable cause) {
        return (eventIds, status) -> {
            log.warn("comment-service недоступен, количество комментариев считаем нулевым: {}", cause.getMessage());
            return Map.of();
        };
    }
}
