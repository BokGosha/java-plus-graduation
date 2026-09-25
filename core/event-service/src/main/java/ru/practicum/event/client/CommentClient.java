package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

@FeignClient(name = "comment-service", path = "/internal/comments")
public interface CommentClient {

    @GetMapping
    Map<Long, Long> getCommentsCountByEventIds(@RequestParam("eventIds") Collection<Long> eventIds,
                                               @RequestParam("status") String status);
}
