package teamfive.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.grpc.stats.message.RecommendedEventProto;
import teamfive.client.analyzer.RecommendationsClient;
import teamfive.dto.event.EventShortDto;
import teamfive.event.mapper.EventMapper;
import teamfive.event.storage.EventRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRecommendationServiceImpl implements EventRecommendationService {

    private final RecommendationsClient recommendationsClient;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> getEventsRecommendations(Long userId, int maxResults) {
        List<Long> ids = recommendationsClient.getRecommendationsForUser(userId, maxResults)
                .map(RecommendedEventProto::getEventId)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return eventRepository.findByIdIn(ids)
                .stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }
}
