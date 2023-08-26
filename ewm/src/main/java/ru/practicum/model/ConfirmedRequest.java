package ru.practicum.model;

import lombok.*;

@Getter
@Setter
public class ConfirmedRequest {
    private Long count;
    private Long eventId;

    public ConfirmedRequest(Long eventId, Long count) {
        this.eventId = eventId;
        this.count = count;

    }
}
