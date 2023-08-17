package ru.practicum.service;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventService {

    List<EventShortDto> getEventsByPrivate(Long userId, Integer from, Integer size);

    EventFullDto createByPrivate(NewEventDto eventDto, Long userId);

    EventFullDto getEventByPrivate(Long userId, Long eventId);

    EventFullDto updateByPrivate(UpdateEventUserRequest updateRequest, Long userId, Long eventId);

    List<ParticipationRequestDto> getRequestsByPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateByPrivate(EventRequestStatusUpdateRequest updateRequest, Long userId, Long eventId);
}

