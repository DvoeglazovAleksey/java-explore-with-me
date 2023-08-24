package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.enums.EventSort;
import ru.practicum.dto.user.UserShortDto;

import java.util.Comparator;
import java.util.Objects;

import static ru.practicum.utils.ExploreDateTimeFormatter.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class EventDto implements Comparable<EventDto> {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private String eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;

    @Override
    public int compareTo(EventDto other) {
        return this.id.compareTo(other.id);
    }

    public static final Comparator<EventDto> EVENT_DATE_COMPARATOR =
            Comparator.comparing((EventDto eventDto) -> stringToLocalDateTime(eventDto.eventDate))
                    .thenComparing(EventDto::getId);

    public static final Comparator<EventDto> VIEWS_COMPARATOR =
            Comparator.comparing(EventDto::getViews)
                    .thenComparing(EventDto::getId);

    public static Comparator<EventDto> getComparator(EventSort sortType) {
        if (Objects.nonNull(sortType) && sortType == EventSort.VIEWS) {
            return VIEWS_COMPARATOR.reversed();
        }
        return EVENT_DATE_COMPARATOR.reversed();
    }
}
