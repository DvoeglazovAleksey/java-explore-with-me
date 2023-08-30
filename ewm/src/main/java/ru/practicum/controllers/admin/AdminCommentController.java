package ru.practicum.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getByEventId(@RequestParam(value = "eventId") Long eventId,
                                         @PositiveOrZero
                                         @RequestParam(value = "from", defaultValue = "0") Integer from,
                                         @Positive
                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return commentService.getByEventIdByAdmin(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getByCommentId(@PathVariable(value = "commentId") Long commentId) {
        return commentService.getByCommentIdByAdmin(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@RequestBody @Valid NewCommentDto newCommentDto,
                             @PathVariable(value = "commentId") Long commentId) {
        return commentService.updateByAdmin(newCommentDto, commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(value = "commentId") Long commentId) {
        commentService.deleteByAdmin(commentId);
    }
}
