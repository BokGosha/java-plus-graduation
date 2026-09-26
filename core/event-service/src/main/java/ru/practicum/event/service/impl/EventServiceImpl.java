package ru.practicum.event.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.client.CommentClient;
import ru.practicum.event.client.RequestClient;
import ru.practicum.event.client.UserClient;
import ru.practicum.event.client.dto.UserShortDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.event.service.StatsHelperService;
import ru.practicum.event.specification.EventSpecification;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final long MIN_HOURS_BEFORE_EVENT = 2L;

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    private final StatsHelperService statsHelperService;

    private final UserClient userClient;
    private final RequestClient requestClient;
    private final CommentClient commentClient;

    @Override
    public List<EventShortDto> getPublicEvents(PublicEventSearchParams params) {
        LocalDateTime start = parseDate(params.rangeStart());
        LocalDateTime end = parseDate(params.rangeEnd());

        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "Field: rangeEnd. Error: rangeEnd должен быть позже rangeStart."
            );
        }

        if (start == null && end == null) {
            start = LocalDateTime.now();
        }

        Pageable pageable = PageRequest.of(
                params.from() / params.size(),
                params.size(),
                Sort.by(Sort.Direction.ASC, "eventDate")
        );

        Specification<Event> specification = EventSpecification.publicFilter(
                normalizeText(params.text()),
                emptyToNull(params.categories()),
                params.paid(),
                start,
                end
        );

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        // Заявки нужны ещё до сборки DTO — по ним отбираются события со свободными местами
        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        if (Boolean.TRUE.equals(params.onlyAvailable())) {
            events = events.stream()
                    .filter(event -> isAvailable(event,
                            confirmedRequests.getOrDefault(event.getId(), 0L)))
                    .toList();
        }

        List<EventShortDto> result = toShortDtos(events, confirmedRequests);
        statsHelperService.hit(params.request());

        if ("VIEWS".equalsIgnoreCase(params.sort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::views).reversed())
                    .toList();
        }

        return result;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        // Сначала фиксируем просмотр, чтобы он вошёл в возвращаемое число views
        statsHelperService.hit(request);

        return toFullDto(event);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        checkUserExists(userId);

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository
                .findByInitiatorId(userId, pageable)
                .getContent();

        return toShortDtos(events);
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        UserShortDto initiator = getUser(userId);
        Category category = getCategory(newEventDto.category());

        if (newEventDto.eventDate()
                .isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new IllegalArgumentException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила."
            );
        }

        Event event = eventMapper.toEntity(newEventDto);

        event.setCategory(category);
        event.setInitiatorId(initiator.id());
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        event = eventRepository.save(event);

        // У нового события ещё нет ни просмотров, ни заявок, ни комментариев
        return toFullDto(event, initiator, 0L, 0L, 0L);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        checkUserExists(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() ->
                        new NotFoundException("Event with id=" + eventId + " was not found"));

        return toFullDto(event);
    }

    @Override
    public EventFullDto updateEventByUser(
            Long userId,
            Long eventId,
            UpdateEventUserRequest updateRequest
    ) {
        checkUserExists(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() ->
                        new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException(
                    "Only pending or canceled events can be changed"
            );
        }

        if (updateRequest.eventDate() != null
                && updateRequest.eventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new IllegalArgumentException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила."
            );
        }

        if (updateRequest.stateAction() != null) {
            switch (updateRequest.stateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            }
        }

        if (updateRequest.annotation() != null) {
            event.setAnnotation(updateRequest.annotation());
        }
        if (updateRequest.category() != null) {
            event.setCategory(getCategory(updateRequest.category()));
        }
        if (updateRequest.description() != null) {
            event.setDescription(updateRequest.description());
        }
        if (updateRequest.eventDate() != null) {
            event.setEventDate(updateRequest.eventDate());
        }
        if (updateRequest.location() != null) {
            if (event.getLocation() == null) {
                event.setLocation(new Location());
            }
            event.getLocation().setLat(updateRequest.location().lat());
            event.getLocation().setLon(updateRequest.location().lon());
        }
        if (updateRequest.paid() != null) {
            event.setPaid(updateRequest.paid());
        }
        if (updateRequest.participantLimit() != null) {
            event.setParticipantLimit(updateRequest.participantLimit());
        }
        if (updateRequest.requestModeration() != null) {
            event.setRequestModeration(updateRequest.requestModeration());
        }
        if (updateRequest.title() != null) {
            event.setTitle(updateRequest.title());
        }

        event = eventRepository.save(event);

        return toFullDto(event);
    }

    @Override
    public List<EventFullDto> searchEventsByAdmin(AdminEventSearchParams params) {
        List<EventState> eventStates = null;

        if (params.states() != null) {
            eventStates = params.states().stream()
                    .map(EventState::valueOf)
                    .toList();
        }

        Pageable pageable = PageRequest.of(
                params.from() / params.size(),
                params.size(),
                Sort.by(Sort.Direction.ASC, "eventDate")
        );

        Specification<Event> specification = EventSpecification.adminFilter(
                params.users(),
                eventStates,
                params.categories(),
                params.rangeStart(),
                params.rangeEnd()
        );

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        return toFullDtos(events);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (updateRequest.annotation() != null) {
            event.setAnnotation(updateRequest.annotation());
        }
        if (updateRequest.description() != null) {
            event.setDescription(updateRequest.description());
        }
        if (updateRequest.title() != null) {
            event.setTitle(updateRequest.title());
        }
        if (updateRequest.category() != null) {
            event.setCategory(getCategory(updateRequest.category()));
        }
        if (updateRequest.paid() != null) {
            event.setPaid(updateRequest.paid());
        }
        if (updateRequest.participantLimit() != null) {
            event.setParticipantLimit(updateRequest.participantLimit());
        }
        if (updateRequest.requestModeration() != null) {
            event.setRequestModeration(updateRequest.requestModeration());
        }
        if (updateRequest.location() != null) {
            if (event.getLocation() == null) {
                event.setLocation(new Location());
            }
            event.getLocation().setLat(updateRequest.location().lat());
            event.getLocation().setLon(updateRequest.location().lon());
        }
        if (updateRequest.eventDate() != null) {
            event.setEventDate(updateRequest.eventDate());
        }

        if (updateRequest.eventDate() != null
                && updateRequest.eventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new IllegalArgumentException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила."
            );
        }

        if (updateRequest.stateAction() != null) {
            switch (updateRequest.stateAction()) {
                case PUBLISH_EVENT -> {
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject the event because it's not in the right state: " + event.getState());
                    }
                    event.setState(EventState.CANCELED);
                }
            }
        }

        event = eventRepository.save(event);

        return toFullDto(event);
    }

    @Override
    public EventInternalDto getInternalEvent(Long eventId) {
        log.info("Getting internal event with id: {}", eventId);

        return eventRepository.findById(eventId)
                .map(eventMapper::toInternalDto)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

    }

    @Override
    public List<EventShortDto> getShortEvents(Collection<Long> eventIds) {
        log.info("Getting short events with ids: {}", eventIds);

        List<Event> events = eventRepository.findByIds(eventIds);

        return toShortDtos(events);
    }

    // Данные из других сервисов для списка собираются одним запросом на сервис, а не на каждое событие
    private List<EventShortDto> toShortDtos(List<Event> events) {
        return toShortDtos(events, getConfirmedRequests(events));
    }

    private List<EventShortDto> toShortDtos(List<Event> events, Map<Long, Long> confirmedRequests) {
        Map<Long, UserShortDto> initiators = getInitiators(events);
        Map<Long, Long> views = statsHelperService.getViews(events);
        Map<Long, Long> commentsCount = getCommentsCount(events);

        return events.stream()
                .map(event -> toShortDto(event,
                        initiators.get(event.getInitiatorId()),
                        views.getOrDefault(event.getId(), 0L),
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        commentsCount.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    private List<EventFullDto> toFullDtos(List<Event> events) {
        Map<Long, UserShortDto> initiators = getInitiators(events);
        Map<Long, Long> views = statsHelperService.getViews(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);
        Map<Long, Long> commentsCount = getCommentsCount(events);

        return events.stream()
                .map(event -> toFullDto(event,
                        initiators.get(event.getInitiatorId()),
                        views.getOrDefault(event.getId(), 0L),
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        commentsCount.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    private EventFullDto toFullDto(Event event) {
        List<Event> events = List.of(event);

        return toFullDto(event,
                getUser(event.getInitiatorId()),
                statsHelperService.getViews(event),
                getConfirmedRequests(events).getOrDefault(event.getId(), 0L),
                getCommentsCount(events).getOrDefault(event.getId(), 0L));
    }

    private EventShortDto toShortDto(Event event,
                                     UserShortDto initiator,
                                     long views,
                                     long confirmedRequests,
                                     long commentsCount) {
        return eventMapper.toShortDto(event, initiator, confirmedRequests, views, commentsCount);
    }

    private EventFullDto toFullDto(Event event,
                                   UserShortDto initiator,
                                   long views,
                                   long confirmedRequests,
                                   long commentsCount) {
        return eventMapper.toFullDto(event, initiator, confirmedRequests, views, commentsCount);
    }

    // Если пользователь удалён, его не будет в ответе — у события initiator останется null
    private Map<Long, UserShortDto> getInitiators(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        return userClient.getUsers(initiatorIds).stream()
                .collect(Collectors.toMap(UserShortDto::id, Function.identity()));
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        return requestClient.getRequestsCountByEventIds(eventIds, "CONFIRMED");
    }

    private Map<Long, Long> getCommentsCount(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        return commentClient.getCommentsCountByEventIds(eventIds, "PUBLISHED");
    }

    private boolean isAvailable(Event event, Long confirmedRequests) {
        Integer limit = event.getParticipantLimit();
        return limit == null
                || limit == 0
                || confirmedRequests < limit;
    }

    private LocalDateTime parseDate(String value) {
        return value == null || value.isBlank()
                ? null
                : LocalDateTime.parse(value, FORMATTER);
    }

    private String normalizeText(String text) {
        return text == null || text.isBlank()
                ? null
                : text;
    }

    private List<Long> emptyToNull(List<Long> values) {
        return values == null || values.isEmpty()
                ? null
                : values;
    }

    private void checkUserExists(Long userId) {
        userClient.getUser(userId);
    }

    private UserShortDto getUser(Long userId) {
        return userClient.getUser(userId);
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new NotFoundException("Category with id=" + categoryId + " was not found"));
    }
}
