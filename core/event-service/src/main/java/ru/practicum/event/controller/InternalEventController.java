package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.service.EventService;
import ru.practicum.event.dto.EventInternalDto;
import ru.practicum.event.dto.EventShortDto;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventInternalDto getEvent(@PathVariable Long eventId) {
        return eventService.getInternalEvent(eventId);
    }

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam("eventIds") Collection<Long> eventIds) {
        return eventService.getShortEvents(eventIds);
    }
}
