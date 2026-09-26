package ru.practicum.comment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.comment.client.dto.EventInternalDto;

@FeignClient(name = "event-service", path = "/internal/events")
public interface EventClient {

    @GetMapping("/{eventId}")
    EventInternalDto getEvent(@PathVariable("eventId") Long eventId);
}
