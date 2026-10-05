package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.client.dto.EventShortDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIds", ignore = true)
    Compilation toEntity(NewCompilationDto newCompilationDto);

    @Mapping(target = "id", source = "compilation.id")
    @Mapping(target = "title", source = "compilation.title")
    @Mapping(target = "pinned", source = "compilation.pinned")
    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation, List<EventShortDto> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIds", ignore = true)
    Compilation updateFromDto(UpdateCompilationRequest updateCompilationRequest);
}
