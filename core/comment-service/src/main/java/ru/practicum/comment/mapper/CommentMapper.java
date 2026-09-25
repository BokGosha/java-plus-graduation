package ru.practicum.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.client.dto.UserShortDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    Comment toEntity(NewCommentDto newCommentDto);

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "author", source = "author")
    CommentDto toDto(Comment comment, UserShortDto author);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    Comment toEntity(UpdateCommentDto updateCommentDto);
}
