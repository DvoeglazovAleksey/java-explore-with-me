package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.EventState;
import ru.practicum.enums.EventStateAction;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.*;
import ru.practicum.service.AdminEventService;
import ru.practicum.service.PrivateEventService;
import ru.practicum.service.PublicEventService;
import ru.practicum.utils.ExploreUrlDecoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.utils.ExploreConstantsAndStaticMethods.*;
import static ru.practicum.utils.ExploreDateTimeFormatter.stringToLocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements AdminEventService, PublicEventService, PrivateEventService {

    private static final List<EventState> STATES_OF_EVENTS_THAT_CAN_BE_UPDATED =  List.of(
            EventState.PENDING,
            EventState.CANCELED,
            EventState.REJECTED
    );

    private static final List<EventState> STATES_OF_EVENTS_THAT_CAN_BE_REJECTED_OR_PUBLISHED =  List.of(
            EventState.PENDING
    );

    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final RequestRepository requestRepo;
    private final CategoryRepository categoryRepo;
    private final LocationRepository locationRepo;

    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final ParticipationRequestMapper requestMapper;

    private final StatService statService;

    @Override
    @Transactional
    public EventFullDto createByPrivate(NewEventDto newEventDto, Long userId) {
        Event newEvent = completeNewEvent(newEventDto, userId);
        checkDateTimeIsAfterNowWithGap(newEvent.getEventDate(), 2);
        Event savedEvent = eventRepo.save(newEvent);
        return completeEventFullDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByPrivate(Long userId, Integer from, Integer size) {
        getUserIfExists(userId);
        return eventRepo.findByInitiatorId(userId, pageRequestOf(from, size))
                .map(eventMapper::toEventShortDto)
                .map(this::completeEventShortDto)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByPrivate(Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        return completeEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateByPrivate(UpdateEventUserRequest request, Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        checkEventStateInList(event.getState(), STATES_OF_EVENTS_THAT_CAN_BE_UPDATED);
        updateEventWithRequest(event, request);
        eventRepo.save(event);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByPrivate(Long userId, Long eventId) {
        getUserIfExists(userId);
        return requestRepo.findByEventId(eventId)
                .stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateByPrivate(EventRequestStatusUpdateRequest update, Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        List<Long> requestIds = update.getRequestIds();
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        if (!isRequestStatusUpdateAllowed(event, update)) {
            return result;
        }

        List<ParticipationRequest> requestsToUpdate = getRequestListByIds(requestIds);
        checkAllRequestsPending(requestsToUpdate);
        RequestStatus status = RequestStatus.valueOf(update.getStatus());

        if (status == RequestStatus.CONFIRMED) {
            confirmAndSetInResult(requestsToUpdate, result, event);
        } else if (status == RequestStatus.REJECTED) {
            rejectAndSetInResult(requestsToUpdate, result);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsByAdmin(EventFilterParamsDto paramsDto) {
        EventFilterParams params = convertInputParams(paramsDto);
        return eventRepo.adminEventsSearch(params)
                .stream()
                .map(eventMapper::toEventFullDto)
                .map(this::completeWithRequests)
                .map(this::completeWithViews)
                .map(eventDto -> (EventFullDto) eventDto)
                .sorted(getComparator(params.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest request, Long eventId) {
        Event event = getEventIfExists(eventId);

        LocalDateTime actual = event.getEventDate();
        checkDateTimeIsAfterNowWithGap(actual, 1);

        LocalDateTime target = stringToLocalDateTime(request.getEventDate());
        if (Objects.nonNull(target)) {
            checkDateTimeIsAfterNowWithGap(target, 2);
        }

        EventStateAction action = request.getStateAction();
        if (Objects.nonNull(action)) {
            switch (action) {
                case PUBLISH_EVENT:
                    publishEvent(request, event);
                    break;
                case REJECT_EVENT:
                    rejectEvent(event);
                    break;
            }
        }
        return eventMapper.toEventFullDto(eventRepo.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventsByPublic(Long eventId, HttpServletRequest request) {
        Event event = getEventIfExists(eventId);
        checkEventIsPublished(event.getState());
        EventFullDto eventFullDto = completeEventFullDto(event);
        statService.addHit(request);
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByPublic(EventFilterParamsDto paramsDto, HttpServletRequest request) {
        EventFilterParams params = convertInputParams(paramsDto);
        List<Event> events = eventRepo.publicEventsSearch(params);
        List<EventDto> eventDtos = events.stream()
                .map(eventMapper::toEventShortDto)
                .peek(this::completeWithRequests)
                .peek(this::completeWithViews)
                .sorted(getComparator(params.getSort()))
                .collect(Collectors.toList());
        statService.addHit(request);
        return eventMapper.toEventShortDtoList(eventDtos);
    }

    private Comparator<EventDto> getComparator(EventSort sortType) {
        return EventDto.getComparator(sortType);
    }

    private void checkEventStateInList(EventState state, List<EventState> validStates) {
        if (!validStates.contains(state)) {
            throw new ConflictException(INVALID_EVENT_STATUS);
        }
    }

    private boolean isRequestStatusUpdateAllowed(Event event, EventRequestStatusUpdateRequest update) {
        return event.getRequestModeration() &&
                event.getParticipantLimit() > 0 &&
                !update.getRequestIds().isEmpty();
    }

    private List<ParticipationRequest> getRequestListByIds(List<Long> requestIds) {
        return requestRepo.findAllByIdIn(requestIds);
    }

    private EventDto completeWithRequests(EventDto eventDto) {
        Long eventId = eventDto.getId();
        Long confirmedRequests = requestRepo.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        eventDto.setConfirmedRequests(confirmedRequests);
        return eventDto;
    }

    private EventDto completeWithViews(EventDto eventDto) {
        Long eventId = eventDto.getId();
        Long views = statService.getViews(eventId);
        eventDto.setViews(views);
        return eventDto;
    }

    private static void checkAllRequestsPending(List<ParticipationRequest> requests) {
        boolean allPending = requests.stream()
                .allMatch(r -> r.getStatus() == RequestStatus.PENDING);
        if (!allPending) {
            throw new ConflictException(EVENT_REQUEST_STATUS_CHANGE_FORBIDDEN);
        }
    }

    private void checkEventIsPublished(EventState state) {
        boolean published = (state == EventState.PUBLISHED);
        if (!published) {
            throw new NotFoundException(EVENT_NOT_FOUND_EXCEPTION);
        }
    }

    private EventFilterParams convertInputParams(EventFilterParamsDto paramsDto) {
        EventFilterParams params;
        try {
            String startString = paramsDto.getRangeStart();
            String endString = paramsDto.getRangeEnd();
            LocalDateTime start = getFromStringOrSetDefault(startString, LocalDateTime.now());
            LocalDateTime end = getFromStringOrSetDefault(endString, null);
            if (end != null && end.isBefore(start)) {
                throw new BadRequestException(EVENT_INCORRECT_TIME_RANGE_FILTER);
            }
            params = eventMapper.toEventFilterParams(paramsDto,start, end);
        } catch (UnsupportedEncodingException e) {
            throw new ConflictException(EVENT_SEARCH_INVALID_PARAMETERS);
        }
        return params;
    }

    private static LocalDateTime getFromStringOrSetDefault(String dateTimeString, LocalDateTime defaultValue) throws UnsupportedEncodingException {
        if (dateTimeString != null) {
            return ExploreUrlDecoder.urlStringToLocalDateTime(dateTimeString);
        }
        return defaultValue;
    }

    private void publishEvent(UpdateEventAdminRequest request, Event event) {
        checkEventStateInList(event.getState(), STATES_OF_EVENTS_THAT_CAN_BE_REJECTED_OR_PUBLISHED);
        updateEventFields(event, request);
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        checkEventStateInList(event.getState(), STATES_OF_EVENTS_THAT_CAN_BE_REJECTED_OR_PUBLISHED);
        event.setState(EventState.REJECTED);
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result, Event event) {
        long confirmed = requestRepo.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long limit = event.getParticipantLimit();

        for (ParticipationRequest request : requestsToUpdate) {
            if (confirmed == limit) {
                int start = requestsToUpdate.indexOf(request);
                int end = requestsToUpdate.size();
                rejectAndSetInResult(requestsToUpdate.subList(start, end), result);
                throw new ConflictException(EVENT_PARTICIPANTS_LIMIT_IS_REACHED);
            }
            confirmAndSetInResult(List.of(request), result);
            confirmed++;
        }
    }

    private void rejectAndSetInResult(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.REJECTED);
        List<ParticipationRequest> rejectedRequests = requestRepo.saveAll(requestsToUpdate);
        result.setRejectedRequests(requestMapper.toRequestDtoList(rejectedRequests));
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.CONFIRMED);
        List<ParticipationRequest> confirmed = requestRepo.saveAll(requestsToUpdate);
        result.setConfirmedRequests(requestMapper.toRequestDtoList(confirmed));
    }

    private void setStatus(List<ParticipationRequest> requestsToUpdate, RequestStatus status) {
        requestsToUpdate.forEach(r -> r.setStatus(status));
    }

    private User getUserIfExists(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepo.findById(eventId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND_EXCEPTION));
    }

    private Category getCategoryIfExists(Long catId) {
        return categoryRepo.findById(catId)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_EXCEPTION));
    }

    private Location getLocation(LocationDto locationDto) {
        Location location = locationMapper.toLocation(locationDto);
        return locationRepo.getByLatAndLon(location.getLat(), location.getLon())
                .orElse(locationRepo.save(location));
    }

    private EventShortDto completeEventShortDto(EventShortDto eventShortDto) {
        Long eventId = eventShortDto.getId();
        Long confirmedRequests = getConfirmedRequests(eventId);
        Long views = statService.getViews(eventId);
        eventShortDto.setConfirmedRequests(confirmedRequests);
        eventShortDto.setViews(views);
        return eventShortDto;
    }

    private Event completeNewEvent(NewEventDto newEventDto, Long userId) {
        Event event = eventMapper.toEvent(newEventDto);
        User user = getUserIfExists(userId);
        Category category = getCategoryIfExists(newEventDto.getCategory());
        Location location = getLocation(newEventDto.getLocation());
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(EventState.PENDING);
        return event;
    }

    private EventFullDto completeEventFullDto(Event event) {
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        Long confirmedRequests = getConfirmedRequests(event.getId());
        completeWithViews(eventFullDto);
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestRepo.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private void updateEventWithRequest(Event event, UpdateEventUserRequest request) {
        updateEventFields(event, request);
        updateEventStateAction(event, request.getStateAction());
    }

    private void updateEventFields(Event event, UpdateEventRequest request) {
        updateEventAnnotation(event, request.getAnnotation());
        updateEventCategory(event, request.getCategory());
        updateEventDescription(event, request.getDescription());
        updateEventDate(event, request.getEventDate());
        updateEventLocation(event, request.getLocation());
        updateEventPaidStatus(event, request.getPaid());
        updateEventParticipationLimit(event, request.getParticipantLimit());
        updateEventRequestModeration(event, request.getRequestModeration());
        updateEventTitle(event, request.getTitle());
    }

    private void updateEventTitle(Event event, String title) {
        if (Objects.nonNull(title)) {
            event.setTitle(title);
        }
    }

    private void updateEventStateAction(Event event, EventStateAction action) {
        if (Objects.nonNull(action)) {
            if (action == EventStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (action == EventStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private void updateEventRequestModeration(Event event, Boolean requestModeration) {
        if (Objects.nonNull(requestModeration)) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateEventParticipationLimit(Event event, Long limit) {
        if (Objects.nonNull(limit)) {
            event.setParticipantLimit(limit);
        }
    }

    private void updateEventPaidStatus(Event event, Boolean paid) {
        if (Objects.nonNull(paid)) {
            event.setPaid(paid);
        }
    }

    private void updateEventLocation(Event event, LocationDto locationDto) {
        if (Objects.nonNull(locationDto)) {
            Location updatedLocation = getLocation(locationDto);
            event.setLocation(updatedLocation);
        }
    }

    private void updateEventDate(Event event, String eventDate) {
        if (Objects.nonNull(eventDate)) {
            LocalDateTime updatedEventDate = stringToLocalDateTime(eventDate);
            if (Objects.nonNull(updatedEventDate)) {
                checkDateTimeIsAfterNowWithGap(updatedEventDate, 1);
                event.setEventDate(updatedEventDate);
            }
        }
    }

    private void updateEventDescription(Event event, String description) {
        if (Objects.nonNull(description)) {
            event.setDescription(description);
        }
    }

    private void updateEventCategory(Event event, Long catId) {
        if (Objects.nonNull(catId)) {
            Category updated = getCategoryIfExists(catId);
            event.setCategory(updated);
        }
    }

    private void updateEventAnnotation(Event event, String annotation) {
        if (Objects.nonNull(annotation)) {
            event.setAnnotation(annotation);
        }
    }
}