package teamfive.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import teamfive.dto.event.EventResponseDto;
import teamfive.dto.event.EventShortDto;
import teamfive.event.service.EventRecommendationService;
import teamfive.event.service.EventService;
import teamfive.event.service.UserActionSender;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;
    private final EventRecommendationService eventRecommendationService;
    private final UserActionSender userActionSender;

    //private final StatClient client;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /events: text={}, categories={}, paid={}, sort={}, from={}, size={}",
                text, categories, paid, sort, from, size);
        /*
            При обработке запроса к эндпоинту GET /events теперь не нужно отправлять информацию о просмотре.
         */

        return eventService.getEventsByPublic(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventResponseDto getEvent(@PathVariable Long id,
                                     @RequestHeader("X-EWM-USER-ID") long userId) {
        log.info("GET /events/{}", id);
        /*
            При обработке запроса к эндпоинту GET /events/{id}
             необходимо отправить информацию о просмотре пользователем мероприятия с идентификатором id
         */
        userActionSender.sendView(userId, id);
        return eventService.getEventById(id);
    }

    @GetMapping("recommendations")
    public List<EventShortDto> getEventsRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId,
                                                        @RequestParam(defaultValue = "10") int maxResults) {
        List<EventShortDto> recommendations = eventRecommendationService.getEventsRecommendations(userId, maxResults);
        log.info("Отправлен ответ GET /events/recommendations пользователю {} с телом: {}",
                userId, recommendations);
        return recommendations;
    }

    @PutMapping("/{eventId}/like")
    public void addLikeToEvent(@PathVariable Long eventId,
                               @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Пришел PUT запрос /events/{}/like от пользователя {}", eventId, userId);
        userActionSender.sendLike(userId, eventId);
        log.info("Обработан PUT запрос /events/{}/like от пользователя {}", eventId, userId);
    }

}