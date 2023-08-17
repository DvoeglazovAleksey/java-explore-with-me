package ru.practicum.service;

import ru.practicum.dto.event.EventFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;

import java.util.List;

public interface AdminEventService {

    List<EventFullDto> getEventsByAdmin(EventFilterParamsDto params);

    EventFullDto updateEventByAdmin(UpdateEventAdminRequest request, Long eventId);
}

