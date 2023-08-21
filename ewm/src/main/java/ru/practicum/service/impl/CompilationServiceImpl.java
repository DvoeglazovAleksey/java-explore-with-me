package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.error.ConflictException;
import ru.practicum.error.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CompilationService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepo;
    private final EventRepository eventRepo;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto add(NewCompilationDto newCompDto) {
        List<Event> events = fetchEvents(newCompDto.getEvents());
        Compilation newComp = compilationMapper.toCompilation(newCompDto, events);
        Compilation savedComp = compilationRepo.save(newComp);
        return compilationMapper.toCompilationDto(savedComp);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = getCompilationById(compId);
        if (!request.getEvents().isEmpty()) {
            List<Event> updatedEvents = fetchEvents(request.getEvents());
            compilation.setEvents(updatedEvents);
        }
        if (Objects.nonNull(request.getPinned())) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getTitle() != null) {
            if (compilationRepo.existsByTitleAndIdNot(request.getTitle(), compilation.getId())) {
                throw new ConflictException("Compilation title already exists and could not be used");
            }
            compilation.setTitle(request.getTitle());
        }
        Compilation updatedComp = compilationRepo.save(compilation);
        return compilationMapper.toCompilationDto(updatedComp);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation compilation = getCompilationById(compId);
        compilationRepo.delete(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        Page<Compilation> compilations = compilationRepo.findAllByPinned(pinned, PageRequest.of(from / size, size));
        return compilations.map(compilationMapper::toCompilationDto).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compId) {
        Compilation compilation = getCompilationById(compId);
        return compilationMapper.toCompilationDto(compilation);
    }

    private Compilation getCompilationById(Long comId) {
        return compilationRepo.findById(comId)
                .orElseThrow(() -> new NotFoundException("Compilation not found."));
    }

    private List<Event> fetchEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        return eventRepo.findAllById(eventIds);
    }
}
