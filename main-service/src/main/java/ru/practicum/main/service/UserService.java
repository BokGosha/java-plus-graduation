package ru.practicum.main.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(List<Long> ids, Pageable pageable);

    UserDto registerUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);
}
