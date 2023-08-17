package ru.practicum.repository;

import ru.practicum.dto.event.EventFilterParams;
import ru.practicum.model.Event;

import java.util.List;

public interface CustomEventRepository {

    List<Event> adminEventsSearch(EventFilterParams params);

    List<Event> publicEventsSearch(EventFilterParams params);
}
