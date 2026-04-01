package teamfive.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import teamfive.dto.event.EventInternalDto;
import teamfive.event.service.EventService;
import teamfive.feignclient.event.EventServiceClient;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events") // Тот самый приватный префикс
public class InternalEventController implements EventServiceClient {

    private final EventService eventService;

    @Override
    @GetMapping("/{eventId}")
    public EventInternalDto getEventInternalById(@PathVariable("eventId") Long eventId) {
        return eventService.getEventInternalById(eventId);
    }

    @Override
    @PutMapping("/{eventId}/confirmed")
    public void incrementConfirmedRequests(
            @PathVariable("eventId") Long eventId,
            @RequestParam("count") Integer count) {
        eventService.incrementConfirmedRequests(eventId, count);
    }
}
