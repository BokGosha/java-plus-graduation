package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.service.RequestService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
public class InternalRequestController {

    private final RequestService requestService;

    @GetMapping
    public Map<Long, Long> getRequestsCountByEventIds(@RequestParam("eventIds") Collection<Long> eventIds,
                                                      @RequestParam("status") String status) {
        return requestService.getRequestsCountByEventIds(eventIds, status);
    }

    // Нужен event-service: лайк можно поставить только посещённому мероприятию
    @GetMapping("/confirmed")
    public boolean isRequestConfirmed(@RequestParam("userId") Long userId,
                                      @RequestParam("eventId") Long eventId) {
        return requestService.isRequestConfirmed(userId, eventId);
    }
}
