package ru.practicum.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@UtilityClass
public class ExploreDateTimeFormatter {
    public static String localDateTimeToString(LocalDateTime dateTime) {
        return Objects.isNull(dateTime) ?
                null : dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static LocalDateTime stringToLocalDateTime(String dateTime) {
        if (dateTime != null) {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return null;
    }
}
