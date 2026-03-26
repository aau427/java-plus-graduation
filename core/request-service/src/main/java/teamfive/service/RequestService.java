package teamfive.service;


import teamfive.dto.event.EventRequestStatusUpdateRequest;
import teamfive.dto.request.EventRequestStatusUpdateResult;
import teamfive.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId,
                                                         EventRequestStatusUpdateRequest updateRequest);
}
