package ru.practicum.utils;

import lombok.experimental.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@UtilityClass
public class ExploreDateTimeFormatter {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public String localDateTimeToString(LocalDateTime dateTime) {
        return Objects.isNull(dateTime) ?
                null : dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public LocalDateTime stringToLocalDateTime(String dateTime) {
        if (dateTime != null) {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        }
        return null;
    }
}
