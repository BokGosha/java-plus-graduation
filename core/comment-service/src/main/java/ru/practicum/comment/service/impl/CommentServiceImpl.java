package ru.practicum.comment.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.CommentService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.comment.client.EventClient;
import ru.practicum.comment.client.UserClient;
import ru.practicum.comment.client.dto.EventInternalDto;
import ru.practicum.comment.client.dto.UserShortDto;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.comment.client.dto.EventState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    private final UserClient userClient;
    private final EventClient eventClient;

    private final StatsClient statsClient;

    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        UserShortDto user = getUser(userId);
        EventInternalDto event = getEvent(eventId);

        if (event.state() != EventState.PUBLISHED) {
            throw new ConflictException("Only published events can be commented");
        }

        Comment comment = commentMapper.toEntity(newCommentDto);
        comment.setAuthorId(userId);
        comment.setEventId(eventId);
        comment.setStatus(CommentStatus.PENDING);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment, user);
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        checkUserExists(userId);

        Pageable pageable = PageRequest.of(from / size, size);

        return toDtos(commentRepository.findByAuthorId(userId, pageable).getContent());
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        checkUserExists(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NotFoundException("Comment with id=" + commentId + " was not found"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }

        if (comment.getStatus() == CommentStatus.PUBLISHED) {
            throw new ConflictException("Published comment cannot be updated");
        }

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new ConflictException("Deleted comment cannot be updated");
        }

        comment.setText(updateCommentDto.text());
        comment.setUpdated(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);

        Comment updatedComment = commentRepository.save(comment);

        return toDto(updatedComment);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        checkUserExists(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NotFoundException("Comment with id=" + commentId + " was not found"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }

        comment.setStatus(CommentStatus.DELETED);
        comment.setUpdated(LocalDateTime.now());

        commentRepository.save(comment);
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, int from, int size, HttpServletRequest request) {
        checkEventExists(eventId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "created"));

        List<CommentDto> comments = toDtos(commentRepository
                .findByEventIdAndStatus(eventId, CommentStatus.PUBLISHED, pageable).getContent());

        statsClient.hit(request);

        return comments;
    }

    @Override
    public CommentDto getEventComment(Long eventId, Long commentId, HttpServletRequest request) {
        Comment comment = commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() ->
                        new NotFoundException("Comment with id=" + commentId + " was not found"));

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }

        statsClient.hit(request);

        return toDto(comment);
    }

    @Override
    public List<CommentDto> getComments(String status, int from, int size) {
        CommentStatus commentStatus = CommentStatus.from(status);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));

        return toDtos(commentRepository.findAllByStatus(commentStatus, pageable).getContent());
    }

    @Override
    public CommentDto publishComment(Long commentId) {
        Comment comment = getComment(commentId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Only comment with status PENDING can be published");
        }

        comment.setStatus(CommentStatus.PUBLISHED);
        comment.setUpdated(LocalDateTime.now());

        return toDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto rejectComment(Long commentId) {
        Comment comment = getComment(commentId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Only comment with status PENDING can be rejected");
        }

        comment.setStatus(CommentStatus.REJECTED);
        comment.setUpdated(LocalDateTime.now());

        return toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }

        commentRepository.deleteById(commentId);
    }

    @Override
    public Map<Long, Long> getCommentsCountByEventIds(Collection<Long> eventIds, String status) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        CommentStatus commentStatus = CommentStatus.from(status);

        return commentRepository.countByEventIdsAndStatus(eventIds, commentStatus).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NotFoundException("Comment with id=" + commentId + " was not found"));
    }

    private CommentDto toDto(Comment comment) {
        return commentMapper.toDto(comment, getUser(comment.getAuthorId()));
    }

    private List<CommentDto> toDtos(List<Comment> comments) {
        List<Long> authorIds = comments.stream().map(Comment::getAuthorId).distinct().toList();
        Map<Long, UserShortDto> authors = userClient.getUsers(authorIds).stream()
                .collect(Collectors.toMap(UserShortDto::id, Function.identity()));

        return comments.stream()
                .map(c -> commentMapper.toDto(c, authors.get(c.getAuthorId())))
                .toList();
    }

    private UserShortDto getUser(Long userId) {
        return userClient.getUser(userId);
    }

    private EventInternalDto getEvent(Long eventId) {
        return eventClient.getEvent(eventId);
    }

    private void checkUserExists(Long userId) {
        userClient.getUser(userId);
    }

    private void checkEventExists(Long eventId) {
        eventClient.getEvent(eventId);
    }
}
