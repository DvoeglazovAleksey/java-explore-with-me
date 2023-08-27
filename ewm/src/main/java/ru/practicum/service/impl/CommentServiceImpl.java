package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentsRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.CommentService;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentsRepository commentsRepository;
    private final CommentMapper commentMapper;
    private final EntityManager entityManager;

    @Override
    public CommentDto add(NewCommentDto newCommentDto, Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found or unavailable."));
        Event event = getEventIfExists(eventId);
        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        comment.setText(newCommentDto.getText());
        return commentMapper.toCommentDto(commentsRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateByAuthorId(NewCommentDto newCommentDto, Long userId, Long commentId) {
        Comment oldComment = getCommentIfExists(commentId);
        checkUser(userId);
        if (!oldComment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Can't delete comment, if his owner another user");
        }
        oldComment.setText(newCommentDto.getText());
        Comment savedComment = commentsRepository.save(oldComment);
        log.debug("Comment with id = {} was update", commentId);
        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    public CommentDto updateByAdmin(NewCommentDto newCommentDto, Long commentId) {
        Comment oldComment = getCommentIfExists(commentId);
        oldComment.setText(newCommentDto.getText());
        Comment savedComment = commentsRepository.save(oldComment);
        log.debug("Comment with id = {} was update", commentId);
        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    public CommentDto getByCommentId(Long userId, Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        checkUser(userId);
        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConflictException("Can't get comment created by another user");
        }
        log.debug("Get comment with id = {}", commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getAllByCreateTime(Long userId, LocalDateTime createStart, LocalDateTime createEnd, Integer from, Integer size) {
        checkUser(userId);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Comment> query = builder.createQuery(Comment.class);
        Root<Comment> root = query.from(Comment.class);
        if (createStart != null && createEnd != null) {
            if (createEnd.isBefore(createStart)) {
                throw new ConflictException("createEnd must be after createStart");
            }
        }
        Predicate criteria = root.get("author").in(userId);
        if (createStart != null) {
            Predicate greaterTime = builder.greaterThanOrEqualTo(
                    root.get("created").as(LocalDateTime.class), createStart);
            criteria = builder.and(criteria, greaterTime);
        }
        if (createEnd != null) {
            Predicate lessTime = builder.lessThanOrEqualTo(
                    root.get("created").as(LocalDateTime.class), createEnd);
            criteria = builder.and(criteria, lessTime);
        }
        query.select(root).where(criteria).orderBy(builder.asc(root.get("created")));
        List<Comment> comments = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
        log.debug("Get comment`s list of user with id = {}", userId);
        return commentMapper.toCommentDtos(comments);
    }

    @Override
    public void deleteByAuthorId(Long userId, Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        checkUser(userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Can't delete comment, if his owner another user");
        }
        log.debug("Comment with ID = {} was delete", commentId);
        commentsRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getByEventIdByAdmin(Long eventId, Integer from, Integer size) {
        getEventIfExists(eventId);
        Pageable page = PageRequest.of(from / size, size);
        List<Comment> eventComments = commentsRepository.findAllByEvent_Id(eventId, page);
        log.debug("Get comment`s list of event with ID = {}", eventId);
        return commentMapper.toCommentDtos(eventComments);
    }

    @Override
    public CommentDto getByCommentIdByAdmin(Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        log.debug("Comment with ID = {} was found", commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public void deleteByAdmin(Long commentId) {
        commentsRepository.deleteById(commentId);
        log.debug("Comment with ID = {} was delete", commentId);
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found or unavailable.");
        }
    }

    private Comment getCommentIfExists(Long commentId) {
        return commentsRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found or unavailable."));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found or unavailable."));
    }
}
