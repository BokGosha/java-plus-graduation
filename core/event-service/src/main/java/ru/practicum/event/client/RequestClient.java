package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestClient {

    @GetMapping
    Map<Long, Long> getRequestsCountByEventIds(@RequestParam("eventIds") Collection<Long> eventIds,
                                               @RequestParam("status") String status);
}
