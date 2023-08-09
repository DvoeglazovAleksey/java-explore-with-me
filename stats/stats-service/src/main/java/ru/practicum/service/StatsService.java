package ru.practicum.service;

import ru.practicum.HitDto;
import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void save(HitDto hitDto);

    List<Stats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    List<Stats> getUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);
}
