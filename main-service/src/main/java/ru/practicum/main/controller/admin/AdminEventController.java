package ru.practicum.main.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.AdminEventSearchParams;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.UpdateEventAdminRequest;
import ru.practicum.main.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> searchEvents(AdminEventSearchParams params) {
        log.info("GET /admin/events - users={}, states={}, categories={}",
                params.getUsers(), params.getStates(), params.getCategories());
        return eventService.searchEventsByAdmin(params);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest updateRequest) {
        log.info("PATCH /admin/events/{}", eventId);
        return eventService.updateEventByAdmin(eventId, updateRequest);
    }
}