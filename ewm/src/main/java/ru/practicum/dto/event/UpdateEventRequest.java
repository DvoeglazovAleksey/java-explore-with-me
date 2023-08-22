package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import ru.practicum.dto.location.LocationDto;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime eventDate;
    protected LocationDto location;
    protected Boolean paid;
    @Min(0)
    protected Long participantLimit;
    protected Boolean requestModeration;
    @Length(min = 3, max = 120)
    protected String title;
}
