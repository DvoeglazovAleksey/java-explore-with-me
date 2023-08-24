package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.enums.RequestStatus;
import ru.practicum.model.ConfirmedRequest;
import ru.practicum.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    @Query("select new ru.practicum.model.ConfirmedRequest(r.event.id,COUNT(distinct r)) " +
            "FROM requests r " +
            "where r.status = 'CONFIRMED' and r.event.id IN :eventsIds group by r.event.id")
    List<ConfirmedRequest> findConfirmedRequest(@Param(value = "eventsIds") List<Long> eventsIds);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByRequesterId(Long userId);

    Optional<ParticipationRequest> findFirst1ByEventIdAndRequesterId(Long eventId, Long userId);

    List<ParticipationRequest> findAllByIdIn(List<Long> ids);
}
