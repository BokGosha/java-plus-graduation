package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RequestService {

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(Long userId,
                                                        Long eventId,
                                                        EventRequestStatusUpdateRequest updateRequest);

    Map<Long, Long> getRequestsCountByEventIds(Collection<Long> eventIds, String status);
}
