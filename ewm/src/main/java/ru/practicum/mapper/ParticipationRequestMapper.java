package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {
    @Mapping(target = "event", expression = "java(request.getEvent().getId())")
    @Mapping(target = "created", expression = "java(ru.practicum.utils.ExploreDateTimeFormatter.localDateTimeToString(request.getCreated()))")
    @Mapping(target = "requester", expression = "java(request.getRequester().getId())")
    @Mapping(target = "status", expression = "java(request.getStatus().name())")
    ParticipationRequestDto toRequestDto(ParticipationRequest request);

    List<ParticipationRequestDto> toRequestDtoList(List<ParticipationRequest> requests);
}
