package ru.practicum.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(List<Long> userIds, Pageable pageable);

    UserDto registerUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

    UserShortDto getShortUser(Long userId);

    List<UserShortDto> getShortUsers(List<Long> userIds);
}
