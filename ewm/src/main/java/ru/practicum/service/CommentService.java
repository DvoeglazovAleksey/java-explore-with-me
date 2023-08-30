package ru.practicum.service;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto add(NewCommentDto newCommentDto, Long userId, Long eventId);

    CommentDto updateByAuthorId(NewCommentDto newCommentDto, Long userId, Long commentId);

    CommentDto updateByAdmin(NewCommentDto newCommentDto, Long commentId);

    CommentDto getByCommentId(Long userId, Long commentId);

    List<CommentDto> getAllByCreateTime(Long userId, LocalDateTime createStart, LocalDateTime createEnd, Integer from, Integer size);

    void deleteByAuthorId(Long userId, Long commentId);

    List<CommentDto> getByEventIdByAdmin(Long eventId, Integer from, Integer size);

    CommentDto getByCommentIdByAdmin(Long commentId);

    void deleteByAdmin(Long commentId);
}
