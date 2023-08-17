package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.enums.EventStateAction;
import ru.practicum.dto.location.LocationDto;

import javax.validation.constraints.Min;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UpdateEventRequest {

    @Length(min = 20, max = 2000)
    protected String annotation;
    protected Long category;
    @Length(min = 20, max = 7000)
    protected String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected String eventDate;
    protected LocationDto location;
    protected Boolean paid;
    @Min(0)
    protected Long participantLimit;
    protected Boolean requestModeration;
    protected EventStateAction stateAction;
    @Length(min = 3, max = 120)
    protected String title;
}
