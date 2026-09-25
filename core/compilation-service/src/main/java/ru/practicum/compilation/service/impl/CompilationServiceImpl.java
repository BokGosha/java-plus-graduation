package ru.practicum.compilation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.exception.NotFoundException;
import ru.practicum.compilation.client.EventClient;
import ru.practicum.compilation.client.dto.EventShortDto;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;

    private final EventClient eventClient;

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        log.info("Adding new compilation: {}", newCompilationDto.title());

        Compilation compilation = compilationMapper.toEntity(newCompilationDto);
        compilation.setPinned(newCompilationDto.pinned());

        if (newCompilationDto.events() != null) {
            compilation.setEventIds(new HashSet<>(newCompilationDto.events()));
        } else {
            compilation.setEventIds(new HashSet<>());
        }

        compilation = compilationRepository.save(compilation);

        return toDto(compilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id: {}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (updateRequest.title() != null) {
            compilation.setTitle(updateRequest.title());
        }
        if (updateRequest.pinned() != null) {
            compilation.setPinned(updateRequest.pinned());
        }
        if (updateRequest.events() != null) {
            compilation.setEventIds(new HashSet<>(updateRequest.events()));
        }

        compilation = compilationRepository.save(compilation);
        return toDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);

        List<Compilation> compilations = pinned == null
                ? compilationRepository.findAll(pageRequest).getContent()
                : compilationRepository.findAllByPinned(pinned, pageRequest).getContent();

        return compilations.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        return toDto(compilation);
    }

    private CompilationDto toDto(Compilation compilation) {
        return compilationMapper.toDto(compilation, getEvents(compilation.getEventIds()));
    }

    private List<EventShortDto> getEvents(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        return eventClient.getEvents(eventIds);
    }
}
