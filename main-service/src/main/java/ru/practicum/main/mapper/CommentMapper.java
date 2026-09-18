package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.NewCommentDto;
import ru.practicum.main.dto.UpdateCommentDto;
import ru.practicum.main.model.Comment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "author", ignore = true)
    Comment toEntity(NewCommentDto newCommentDto);

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "author", target = "author")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "author", ignore = true)
    Comment toEntity(UpdateCommentDto updateCommentDto);
}
