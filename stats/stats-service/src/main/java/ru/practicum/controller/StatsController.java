package ru.practicum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitDto;
import ru.practicum.model.Stats;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class StatsController {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @PostMapping("/hit")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void saveHit(@Valid @RequestBody HitDto hitDto) {
        service.save(hitDto);
    }

    @GetMapping("/stats")
    public List<Stats> getStats(@RequestParam String start,
                                @RequestParam String end,
                                @RequestParam(value = "uris", required = false, defaultValue = "") List<String> uris,
                                @RequestParam(value = "unique", required = false, defaultValue = "false") Boolean unique) {
        LocalDateTime updStart = LocalDateTime.parse(start, FORMAT);
        LocalDateTime updEnd = LocalDateTime.parse(end, FORMAT);
        if (unique) {
            return service.getUniqueStats(updStart, updEnd, uris);
        } else {
            return service.getStats(updStart, updEnd, uris);
        }
    }
}
