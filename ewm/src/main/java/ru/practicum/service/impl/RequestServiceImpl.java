package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.RequestRepository;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.RequestService;

import java.util.List;

import static ru.practicum.utils.ExploreConstantsAndStaticMethods.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepo;
    private final UserRepository userRepo;
    private final EventRepository eventRepo;
    private final ParticipationRequestMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAll(Long userId) {
        getUserIfExists(userId);
        List<ParticipationRequest> requests = requestRepo.findByRequesterId(userId);
        return mapper.toRequestDtoList(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto add(Long userId, Long eventId) {
        if (requestRepo.findFirst1ByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Participation request already exists.");
        }
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found."));
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Event owner not allowed to create request to his own event.");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Invalid event status.");
        }
        if (event.getParticipantLimit() > 0) {
            Long participants = requestRepo.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            Long limit = event.getParticipantLimit();
            if (participants >= limit) {
                throw new ConflictException("Participants limit is reached.");
            }
        }
        ParticipationRequest request = completeNewRequest(userId, event);
        return mapper.toRequestDto(requestRepo.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        getUserIfExists(userId);
        ParticipationRequest request = requestRepo.findById(requestId).orElseThrow(() ->
                new NotFoundException("Participation request not found."));
        request.setStatus(RequestStatus.CANCELED);
        return mapper.toRequestDto(requestRepo.save(request));
    }

    private ParticipationRequest completeNewRequest(Long userId, Event event) {
        User user = getUserIfExists(userId);
        boolean needConfirmation = event.getRequestModeration();
        boolean hasParticipantsLimit = event.getParticipantLimit() != 0;
        RequestStatus status = needConfirmation && hasParticipantsLimit ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        return ParticipationRequest.builder()
                .requester(user)
                .status(status)
                .event(event)
                .build();
    }

    private User getUserIfExists(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }
}
