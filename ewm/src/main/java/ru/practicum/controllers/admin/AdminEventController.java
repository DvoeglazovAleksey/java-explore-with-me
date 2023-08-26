package ru.practicum.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.AdminEventService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService service;

    @GetMapping
    public List<EventFullDto> getByAdmin(@Valid EventFilterParamsDto params) {
        return service.getEventsByAdmin(params);
    }

    @PatchMapping(value = "/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable Long eventId,
                        @Valid @RequestBody UpdateEventAdminRequest request) {
        return service.updateEventByAdmin(request, eventId);
    }
}
