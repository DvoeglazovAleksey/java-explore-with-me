package ru.practicum.controllers.priv;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@Validated
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;
    private static final String PATTERN_DATE = ("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto add(@RequestBody @Valid NewCommentDto newCommentDto,
                          @PathVariable(value = "userId") Long userId,
                          @PathVariable(value = "eventId") Long eventId) {
        return commentService.add(newCommentDto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateByAuthorId(@RequestBody @Valid NewCommentDto newCommentDto,
                                       @PathVariable(value = "userId") Long userId,
                                       @PathVariable(value = "commentId") Long commentId) {
        return commentService.updateByAuthorId(newCommentDto, userId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getByCommentId(@PathVariable(value = "userId") Long userId,
                                     @PathVariable(value = "commentId") Long commentId) {
        return commentService.getByCommentId(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getAllByCreateTime(@PathVariable(value = "userId") Long userId,
                                               @PositiveOrZero
                                               @RequestParam(value = "from", defaultValue = "0") Integer from,
                                               @Positive
                                               @RequestParam(value = "size", defaultValue = "10") Integer size,
                                               @RequestParam(value = "createStart", required = false)
                                               @DateTimeFormat(pattern = PATTERN_DATE)
                                               LocalDateTime createStart,
                                               @RequestParam(value = "createEnd", required = false)
                                               @DateTimeFormat(pattern = PATTERN_DATE)
                                               LocalDateTime createEnd) {
        return commentService.getAllByCreateTime(userId, createStart, createEnd, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAuthorId(@PathVariable(value = "userId") Long userId,
                                 @PathVariable(value = "commentId") Long commentId) {
        commentService.deleteByAuthorId(userId, commentId);
    }
}
