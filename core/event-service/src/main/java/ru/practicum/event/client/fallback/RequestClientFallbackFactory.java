package ru.practicum.event.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.event.client.RequestClient;
import ru.practicum.exception.ServiceUnavailableException;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
public class RequestClientFallbackFactory implements FallbackFactory<RequestClient> {

    @Override
    public RequestClient create(Throwable cause) {
        return new RequestClient() {

            // Количество заявок необязательно: без request-service отдаём события с confirmedRequests = 0
            @Override
            public Map<Long, Long> getRequestsCountByEventIds(Collection<Long> eventIds, String status) {
                log.warn("request-service недоступен, количество заявок считаем нулевым: {}", cause.getMessage());
                return Map.of();
            }

            // А вот подтверждать лайк без проверки заявки нельзя
            @Override
            public boolean isRequestConfirmed(Long userId, Long eventId) {
                throw new ServiceUnavailableException("request-service недоступен", cause);
            }
        };
    }
}
