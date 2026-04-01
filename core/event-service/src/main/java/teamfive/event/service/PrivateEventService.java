package teamfive.event.service;

import teamfive.dto.event.EventRequestDto;
import teamfive.dto.event.EventResponseDto;
import teamfive.dto.event.EventShortDto;
import teamfive.dto.event.EventUpdateRequestDto;

import java.util.List;

public interface PrivateEventService {
    EventResponseDto createEvent(Long userId, EventRequestDto eventRequestDto);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventResponseDto getEventByUser(Long userId, Long eventId);

    EventResponseDto updateEventByUser(Long userId, Long eventId, EventUpdateRequestDto updateRequest);
}
