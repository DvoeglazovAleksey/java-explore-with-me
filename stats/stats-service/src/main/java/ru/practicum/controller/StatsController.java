package ru.practicum.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitDto;
import ru.practicum.model.Stats;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class StatsController {
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
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
    public List<Stats> getStats(@RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime start,
                                @RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime end,
                                @RequestParam(defaultValue = "") List<String> uris,
                                @RequestParam(defaultValue = "false") Boolean unique) {
        if (unique) {
            return service.getUniqueStats(start, end, uris);
        } else {
            return service.getStats(start, end, uris);
        }
    }
}
