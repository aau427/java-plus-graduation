package teamfive.service;

import ru.practicum.ewm.grpc.stats.message.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.message.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.message.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.message.UserPredictionsRequestProto;

import java.util.List;
import java.util.stream.Stream;

public interface RecommendationService {

    Stream<RecommendedEventProto> getInteractionCount(InteractionsCountRequestProto request);

    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

    Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);
}
