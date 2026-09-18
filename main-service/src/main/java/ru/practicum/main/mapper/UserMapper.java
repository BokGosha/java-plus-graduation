package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.dto.UserDto;
import ru.practicum.main.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User toEntity(NewUserRequest newUserRequest);

    UserDto toDto(User user);
}