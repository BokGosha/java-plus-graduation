package ru.practicum.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> userIds, Pageable pageable) {
        log.info("Getting users with ids: {}, pageable: {}", userIds, pageable);

        List<User> users;
        if (userIds == null || userIds.isEmpty()) {
            users = userRepository.findAll(pageable).getContent();
        } else {
            users = userRepository.findAllByIds(userIds, pageable);
        }

        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserDto registerUser(NewUserRequest newUserRequest) {
        log.info("Registering new user: {}", newUserRequest.email());

        User user = userMapper.toEntity(newUserRequest);
        user = userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        userRepository.deleteById(userId);
    }

    @Override
    public UserShortDto getShortUser(Long userId) {
        log.info("Getting short user with id: {}", userId);

        return userRepository.findById(userId)
                .map(userMapper::toShortDto)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    @Override
    public List<UserShortDto> getShortUsers(List<Long> userIds) {
        log.info("Getting short users with ids: {}", userIds);

        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return userRepository.findAllById(userIds).stream()
                .map(userMapper::toShortDto)
                .toList();
    }
}
