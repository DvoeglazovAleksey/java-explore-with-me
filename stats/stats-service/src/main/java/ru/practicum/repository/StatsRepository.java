package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Hit;
import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {
    // Получение статистики с учетом уникальных IP по временному промежутку и URI
    @Query(value = "SELECT NEW ru.practicum.model.Stats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 AND h.uri IN ?3 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Stats> getUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);


    // Получение статистики без учета уникальных IP по временному промежутку и URI
    @Query(value = "SELECT NEW ru.practicum.model.Stats(h.app, h.uri, COUNT(h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 AND h.uri IN ?3 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<Stats> getNotUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    // Получение статистики с учетом уникальных IP по временному промежутку без URI
    @Query(value = "SELECT NEW ru.practicum.model.Stats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Stats> getUniqueStatsWithoutUris(LocalDateTime start, LocalDateTime end);

    // Получение статистики без учета уникальных IP по временному промежутку без URI
    @Query(value = "SELECT NEW ru.practicum.model.Stats(h.app, h.uri, COUNT(h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<Stats> getNotUniqueStatsWithoutUris(LocalDateTime start, LocalDateTime end);
}
