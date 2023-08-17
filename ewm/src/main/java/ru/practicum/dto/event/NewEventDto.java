package ru.practicum.dto.event;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.dto.location.LocationDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class NewEventDto {

    @NotNull
    @Length(min = 20, max = 2000)
    private String annotation;
    @NotNull
    private Long category;
    @Length(min = 20, max = 7000)
    @NotNull
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate;
    @NotNull
    private LocationDto location;
    private Boolean paid = Boolean.FALSE;
    @Min(0)
    private Integer participantLimit = 0;
    private Boolean requestModeration = Boolean.TRUE;
    @Length(min = 3, max = 120)
    private String title;
}
