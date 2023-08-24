package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.practicum.dto.location.LocationDto;

@Getter
@Setter
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class EventFullDto extends EventDto {
    private String createdOn;
    private String description;
    private LocationDto location;
    private Integer participantLimit;
    private String publishedOn;
    private Boolean requestModeration;
    private String state;
}
