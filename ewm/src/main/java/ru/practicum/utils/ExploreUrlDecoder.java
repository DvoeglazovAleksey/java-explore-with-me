package ru.practicum.utils;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static ru.practicum.utils.ExploreDateTimeFormatter.stringToLocalDateTime;

@UtilityClass
public class ExploreUrlDecoder {
    public static LocalDateTime urlStringToLocalDateTime(String input) {
        String decodedDateTimeStr = java.net.URLDecoder.decode(input, StandardCharsets.UTF_8);
        return stringToLocalDateTime(decodedDateTimeStr);
    }
}


