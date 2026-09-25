package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.client.dto.UserShortDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventInternalDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = CategoryMapper.class)
public interface EventMapper {

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "comments", source = "comments")
    EventShortDto toShortDto(Event event, UserShortDto initiator, Long confirmedRequests, Long views, Long comments);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "comments", source = "comments")
    EventFullDto toFullDto(Event event, UserShortDto initiator, Long confirmedRequests, Long views, Long comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    Event toEntity(NewEventDto newEventDto);

    EventInternalDto toInternalDto(Event event);

    List<EventInternalDto> toInternalDtos(List<Event> events);
}
