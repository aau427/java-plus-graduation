package teamfive.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.grpc.stats.message.RecommendedEventProto;
import teamfive.client.analyzer.RecommendationsClient;
import teamfive.dto.event.EventShortDto;
import teamfive.dto.user.UserDto;
import teamfive.event.mapper.EventMapper;
import teamfive.event.model.Event;
import teamfive.event.storage.EventRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRecommendationServiceImpl implements EventRecommendationService {

    private final RecommendationsClient recommendationsClient;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserUtility userUtility;

    @Override
    public List<EventShortDto> getEventsRecommendations(Long userId, int maxResults) {
        List<Long> ids = recommendationsClient.getRecommendationsForUser(userId, maxResults)
                .map(RecommendedEventProto::getEventId)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Event> events = eventRepository.findByIdIn(ids);
        Map<Long, UserDto> usersMap = userUtility.getUsersMap(events);

        return events
                .stream()
                .map(event -> eventMapper.toEventShortDto(event, usersMap))
                .toList();
    }
}
