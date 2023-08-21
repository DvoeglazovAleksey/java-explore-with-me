package ru.practicum.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.enums.EventState;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;
import ru.practicum.error.NotFoundException;
import ru.practicum.StatsClient;
import ru.practicum.ViewStats;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static ru.practicum.utils.ExploreConstantsAndStaticMethods.EVENT_NOT_FOUND_EXCEPTION;

@Service
@RequiredArgsConstructor
public class StatService {

    private final StatsClient statsClient;
    private final EventRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${app.name}")
    private String app;

    public void addHit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        LocalDateTime timestamp = LocalDateTime.now();
        statsClient.addHit(app, uri, ip, timestamp);
    }

    public Long getViews(Long eventId) {
        Event event = getEventIfExists(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            return 0L;
        }
        LocalDateTime start = event.getPublishedOn();
        LocalDateTime end = LocalDateTime.now();
        String uri = "/events/" + event.getId();
        ResponseEntity<Object> response = statsClient.getStats(start, end, List.of(uri), true);
        return extractViews(response);
    }

    private Event getEventIfExists(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND_EXCEPTION));
    }

    private Long extractViews(ResponseEntity<Object> response) {
        try {
            String responseValue = mapper.writeValueAsString(response.getBody());
            List<ViewStats> viewStats = Arrays.asList(mapper.readValue(responseValue, new TypeReference<>(){}));
            return viewStats.isEmpty() ? 0 : viewStats.get(0).getHits();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}