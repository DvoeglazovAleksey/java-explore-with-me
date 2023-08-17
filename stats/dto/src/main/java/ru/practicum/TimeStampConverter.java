package ru.practicum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeStampConverter {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String mapToString(LocalDateTime timestamp) {
        return timestamp.format(FORMATTER);
    }
}
