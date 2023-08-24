package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.*;
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.*;
import ru.practicum.service.AdminEventService;
import ru.practicum.service.PrivateEventService;
import ru.practicum.service.PublicEventService;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.utils.ExploreDateTimeFormatter.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements AdminEventService, PublicEventService, PrivateEventService {
    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final RequestRepository requestRepo;
    private final CategoryRepository categoryRepo;
    private final LocationRepository locationRepo;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final ParticipationRequestMapper participationRequestMapper;
    private final StatService statService;

    @Override
    @Transactional
    public EventFullDto createByPrivate(NewEventDto newEventDto, Long userId) {
        Event event = eventMapper.toEvent(newEventDto);
        User user = getUserIfExists(userId);
        Category category = getCategoryIfExists(newEventDto.getCategory());
        Location location = getLocation(newEventDto.getLocation());
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(EventState.PENDING);
        checkDateTimeIsAfterNowWithGap(event.getEventDate(), 2);
        Event savedEvent = eventRepo.save(event);
        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByPrivate(Long userId, Integer from, Integer size) {
        getUserIfExists(userId);
        int page = from / size;
        List<Event> events = eventRepo.findByInitiatorId(userId, PageRequest.of(page, size));
        statService.getViewsList(events);
        getConfirmedRequest(events);
        return new ArrayList<>(eventMapper.toEventShortDtoListForEvents(events));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByPrivate(Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        getConfirmedRequest(List.of(event));
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateByPrivate(UpdateEventUserRequest request, Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("You cannot update a published event");
        }
        updateEventFields(event, request);
        updateEventStateAction(event, request.getStateAction());
        eventRepo.save(event);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByPrivate(Long userId, Long eventId) {
        getUserIfExists(userId);
        return requestRepo.findByEventId(eventId)
                .stream()
                .map(participationRequestMapper::toRequestDto)
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
        List<ParticipationRequest> requestsToUpdate = requestRepo.findAllByIdIn(requestIds);
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
        List<Event> events = eventRepo.adminEventsSearch(params);
        statService.getViewsList(events);
        getConfirmedRequest(events);
        return events.stream()
                .map(eventMapper::toEventFullDto)
                .sorted(getComparator(params.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest request, Long eventId) {
        Event event = getEventIfExists(eventId);
        LocalDateTime actual = event.getEventDate();
        checkDateTimeIsAfterNowWithGap(actual, 1);
        LocalDateTime target = request.getEventDate();
        if (Objects.nonNull(target)) {
            checkDateTimeIsAfterNowWithGap(target, 2);
        }
        StateActionAdmin action = request.getStateAction();
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
        boolean published = (event.getState() == EventState.PUBLISHED);
        if (!published) {
            throw new NotFoundException("Event not found.");
        }
        EventFullDto eventFullDto = completeEventFullDto(event);
        statService.addHit(request);
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByPublic(EventFilterParamsDto paramsDto, HttpServletRequest request) {
        EventFilterParams params = convertInputParams(paramsDto);
        List<Event> events = eventRepo.publicEventsSearch(params);
        statService.getViewsList(events);
        getConfirmedRequest(events);
        statService.addHit(request);
        return events.stream().map(eventMapper::toEventShortDto)
                .sorted(getComparator(params.getSort())).collect(Collectors.toList());
    }

    private void getConfirmedRequest(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        List<ConfirmedRequest> confirmedRequests = requestRepo.findConfirmedRequest(eventIds);
        Map<Long, Long> confirmedRequestsMap = confirmedRequests.stream()
                .collect(Collectors.toMap(ConfirmedRequest::getEventId, ConfirmedRequest::getCount));
        events.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L)));
    }

    private Comparator<EventDto> getComparator(EventSort sortType) {
        return EventDto.getComparator(sortType);
    }

    private boolean isRequestStatusUpdateAllowed(Event event, EventRequestStatusUpdateRequest update) {
        return event.getRequestModeration() &&
                event.getParticipantLimit() > 0 &&
                !update.getRequestIds().isEmpty();
    }

    private void completeWithViews(EventDto eventDto) {
        Long eventId = eventDto.getId();
        Long views = statService.getViews(eventId);
        eventDto.setViews(views);
    }

    private static void checkAllRequestsPending(List<ParticipationRequest> requests) {
        boolean allPending = requests.stream()
                .allMatch(r -> r.getStatus() == RequestStatus.PENDING);
        if (!allPending) {
            throw new ConflictException("Impossible to change request status.");
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
                throw new BadRequestException("Invalid time-range filter params.");
            }
            params = eventMapper.toEventFilterParams(paramsDto, start, end);
        } catch (UnsupportedEncodingException e) {
            throw new ConflictException("Invalid search parameters.");
        }
        return params;
    }

    private static LocalDateTime getFromStringOrSetDefault(String dateTimeString, LocalDateTime defaultValue) throws UnsupportedEncodingException {
        if (dateTimeString != null) {
            return stringToLocalDateTime(java.net.URLDecoder.decode(dateTimeString, StandardCharsets.UTF_8));
        }
        return defaultValue;
    }

    private void publishEvent(UpdateEventAdminRequest request, Event event) {
        EventState state = event.getState();
        if (state == EventState.PUBLISHED) {
            throw new ConflictException("The event has already been published");
        }
        if (state == EventState.REJECTED) {
            throw new ConflictException("The event has already been rejected");
        }
        if (state == EventState.CANCELED) {
            throw new ConflictException("The event has already been canceled");
        }
        updateEventFields(event, request);
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        EventState state = event.getState();
        if (state == EventState.PUBLISHED || state == EventState.CANCELED) {
            throw new ConflictException("You cannot reject a published event");
        }
        event.setState(EventState.CANCELED);
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result, Event event) {
        long confirmed = requestRepo.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long limit = event.getParticipantLimit();

        for (ParticipationRequest request : requestsToUpdate) {
            if (confirmed == limit) {
                int start = requestsToUpdate.indexOf(request);
                int end = requestsToUpdate.size();
                rejectAndSetInResult(requestsToUpdate.subList(start, end), result);
                throw new ConflictException("Participants limit is reached.");
            }
            confirmAndSetInResult(List.of(request), result);
            confirmed++;
        }
    }

    private void rejectAndSetInResult(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.REJECTED);
        List<ParticipationRequest> rejectedRequests = requestRepo.saveAll(requestsToUpdate);
        result.setRejectedRequests(participationRequestMapper.toRequestDtoList(rejectedRequests));
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.CONFIRMED);
        List<ParticipationRequest> confirmed = requestRepo.saveAll(requestsToUpdate);
        result.setConfirmedRequests(participationRequestMapper.toRequestDtoList(confirmed));
    }

    private void setStatus(List<ParticipationRequest> requestsToUpdate, RequestStatus status) {
        requestsToUpdate.forEach(r -> r.setStatus(status));
    }

    private User getUserIfExists(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found or unavailable."));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepo.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found."));
    }

    private Category getCategoryIfExists(Long catId) {
        return categoryRepo.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }

    private Location getLocation(LocationDto locationDto) {
        Location location = locationMapper.toLocation(locationDto);
        return locationRepo.getByLatAndLon(location.getLat(), location.getLon())
                .orElse(locationRepo.save(location));
    }

    private EventFullDto completeEventFullDto(Event event) {
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        Long confirmedRequests = requestRepo.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        completeWithViews(eventFullDto);
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
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
        if (Objects.nonNull(title) && !title.isBlank()) {
            event.setTitle(title);
        }
    }

    private void updateEventStateAction(Event event, StateActionUser action) {
        if (Objects.nonNull(action)) {
            if (action == StateActionUser.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (action == StateActionUser.CANCEL_REVIEW) {
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

    private void updateEventDate(Event event, LocalDateTime eventDate) {
        if (Objects.nonNull(eventDate)) {
            checkDateTimeIsAfterNowWithGap(eventDate, 1);
            event.setEventDate(eventDate);
        }
    }

    private void updateEventDescription(Event event, String description) {
        if (Objects.nonNull(description) && !description.isBlank()) {
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
        if (Objects.nonNull(annotation) && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
    }

    private void checkDateTimeIsAfterNowWithGap(LocalDateTime value, Integer gapFromNowInHours) {
        LocalDateTime minValidDateTime = LocalDateTime.now().plusHours(gapFromNowInHours);
        if (value.isBefore(minValidDateTime)) {
            throw new BadRequestException("Invalid event date-time.");
        }
    }
}