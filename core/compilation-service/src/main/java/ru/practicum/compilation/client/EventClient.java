package ru.practicum.compilation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.compilation.client.dto.EventShortDto;

import java.util.Collection;
import java.util.List;

@FeignClient(name = "event-service", path = "/internal/events")
public interface EventClient {

    @GetMapping
    List<EventShortDto> getEvents(@RequestParam("eventIds") Collection<Long> eventIds);
}
