package teamfive.event.service;

import teamfive.dto.event.EventShortDto;

import java.util.List;

public interface EventRecommendationService {
    List<EventShortDto> getEventsRecommendations(Long userId, int maxResults);
}
