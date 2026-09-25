package ru.practicum.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.request.client.EventClient;
import ru.practicum.request.client.UserClient;
import ru.practicum.request.client.dto.EventInternalDto;
import ru.practicum.request.client.dto.EventState;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatusAction;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.service.RequestService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.info("User id={} requests participation in event id={}", userId, eventId);

        checkUserExists(userId);
        EventInternalDto event = getEvent(eventId);

        // Инициатор не может подать заявку на своё событие
        if (event.initiatorId().equals(userId)) {
            throw new ConflictException("Event initiator cannot request participation in own event");
        }

        // Нельзя участвовать в неопубликованном событии
        if (event.state() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        // Нельзя подать заявку повторно
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        // Проверка лимита участников (0 = без ограничения)
        int limit = event.participantLimit() == null ? 0 : event.participantLimit();
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (limit > 0 && confirmed >= limit) {
            throw new ConflictException("The participant limit has been reached");
        }

        // Если премодерация выключена или лимит не задан — заявка сразу подтверждается
        boolean autoConfirm = limit == 0 || Boolean.FALSE.equals(event.requestModeration());

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .eventId(event.id())
                .requesterId(userId)
                .status(autoConfirm ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .build();

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting requests of user id={}", userId);

        checkUserExists(userId);

        return requestMapper.toDtoList(requestRepository.findByRequesterId(userId));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("User id={} cancels request id={}", userId, requestId);

        checkUserExists(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }

        request.setStatus(RequestStatus.CANCELED);

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for event id={} of user id={}", eventId, userId);

        EventInternalDto event = getEvent(eventId);
        if (!event.initiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        return requestMapper.toDtoList(requestRepository.findByEventId(eventId));
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest updateRequest) {
        log.info("User id={} updates requests status for event id={}", userId, eventId);

        EventInternalDto event = getEvent(eventId);
        if (!event.initiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        List<ParticipationRequestDto> confirmedList = new ArrayList<>();
        List<ParticipationRequestDto> rejectedList = new ArrayList<>();

        int limit = event.participantLimit() == null ? 0 : event.participantLimit();
        // Если лимита нет или премодерация выключена — подтверждение не требуется
        if (limit == 0 || Boolean.FALSE.equals(event.requestModeration())) {
            return new EventRequestStatusUpdateResult(confirmedList, rejectedList);
        }

        List<Request> requests = requestRepository.findByEventIdAndIdIn(eventId, updateRequest.requestIds());
        // Все заявки должны быть в статусе PENDING
        for (Request request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        // Отклонение — просто переводим все в REJECTED
        if (updateRequest.status() == RequestStatusAction.REJECTED) {
            for (Request request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedList.add(requestMapper.toDto(request));
            }

            requestRepository.saveAll(requests);

            return new EventRequestStatusUpdateResult(confirmedList, rejectedList);
        }

        // Подтверждение с учётом лимита
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (confirmed >= limit) {
            throw new ConflictException("The participant limit has been reached");
        }

        long available = limit - confirmed;
        for (Request request : requests) {
            if (available > 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedList.add(requestMapper.toDto(request));
                available--;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedList.add(requestMapper.toDto(request));
            }
        }

        requestRepository.saveAll(requests);

        if (available == 0) {
            List<Request> pending = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
            for (Request request : pending) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedList.add(requestMapper.toDto(request));
            }

            requestRepository.saveAll(pending);
        }

        return new EventRequestStatusUpdateResult(confirmedList, rejectedList);
    }

    @Override
    public Map<Long, Long> getRequestsCountByEventIds(Collection<Long> eventIds, String status) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());

        // События без заявок в результат не попадают — вызывающий берёт getOrDefault(id, 0L)
        return requestRepository.countByEventIdsAndStatus(eventIds, requestStatus).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private EventInternalDto getEvent(Long eventId) {
        return eventClient.getEvent(eventId);
    }

    private void checkUserExists(Long userId) {
        userClient.getUser(userId);
    }
}
