package teamfive.event.service;

import teamfive.dto.event.EventInternalDto;
import teamfive.dto.event.EventResponseDto;
import teamfive.dto.event.EventShortDto;
import teamfive.dto.event.EventUpdateRequestDto;

import java.util.List;

public interface EventService {

    List<EventResponseDto> getEventsByAdmin(List<Long> users, List<String> states,
                                            List<Long> categories, String rangeStart,
                                            String rangeEnd, int from, int size);

    EventResponseDto updateEventByAdmin(Long eventId, EventUpdateRequestDto updateRequest);

    List<EventShortDto> getEventsByPublic(String text, List<Long> categories,
                                          Boolean paid, String rangeStart,
                                          String rangeEnd, Boolean onlyAvailable,
                                          String sort, int from, int size);

    EventResponseDto getEventById(Long id);

    EventInternalDto getEventInternalById(Long id);

    void incrementConfirmedRequests(Long eventId, Integer count);
}