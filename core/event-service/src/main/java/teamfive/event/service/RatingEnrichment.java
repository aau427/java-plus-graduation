package teamfive.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import teamfive.client.analyzer.RecommendationsClient;
import teamfive.event.model.Event;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RatingEnrichment {

    private final RecommendationsClient recommendationsClient;

    public List<Event> enrichRatings(List<Event> iEventList) {
        Map<Long, Double> ratingsMap = getRatings(iEventList);
        return iEventList.stream()
                .peek(event -> event.setRating(ratingsMap.getOrDefault(event.getId(), 0.0)))
                .toList();
    }

    public Event enrichRating(Event event) {
        Double rating = getRatings(List.of(event)).getOrDefault(event.getId(), 0.0);
        event.setRating(rating);
        return event;
    }

    private Map<Long, Double> getRatings(List<Event> eventList) {
        List<Long> ids = eventList.stream()
                .map(Event::getId)
                .toList();
        return recommendationsClient.getInteractionsCount(ids);
    }
}
