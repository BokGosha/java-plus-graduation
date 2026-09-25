package ru.practicum.comment.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getUserComments(Long userId, int from, int size);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getEventComments(Long eventId, int from, int size, HttpServletRequest request);

    CommentDto getEventComment(Long eventId, Long commentId, HttpServletRequest request);

    List<CommentDto> getComments(String status, int from, int size);

    CommentDto publishComment(Long commentId);

    CommentDto rejectComment(Long commentId);

    void deleteCommentByAdmin(Long commentId);

    Map<Long, Long> getCommentsCountByEventIds(Collection<Long> eventIds, String status);
}
