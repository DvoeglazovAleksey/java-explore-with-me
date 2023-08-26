package ru.practicum.controllers.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.PublicEventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final PublicEventService service;

    @GetMapping
    public List<EventShortDto> get(@Valid EventFilterParamsDto params, HttpServletRequest request) {
        return service.getEventsByPublic(params, request);
    }

    @GetMapping(value = "/{id}")
    public EventFullDto get(@PathVariable Long id, HttpServletRequest request) {
        return service.getEventsByPublic(id, request);
    }
}
