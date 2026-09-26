package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.service.UserService;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public UserShortDto getUser(@PathVariable Long userId) {
        return userService.getShortUser(userId);
    }

    @GetMapping
    public List<UserShortDto> getUsers(@RequestParam List<Long> userIds) {
        return userService.getShortUsers(userIds);
    }
}
