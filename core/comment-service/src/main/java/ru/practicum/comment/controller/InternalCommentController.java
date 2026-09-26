package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.service.CommentService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/comments")
public class InternalCommentController {

    private final CommentService commentService;

    @GetMapping
    public Map<Long, Long> getCommentsCountByEventIds(@RequestParam("eventIds") Collection<Long> eventIds,
                                                      @RequestParam("status") String status) {
        return commentService.getCommentsCountByEventIds(eventIds, status);
    }
}
