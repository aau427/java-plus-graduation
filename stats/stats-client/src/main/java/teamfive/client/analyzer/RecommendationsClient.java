package teamfive.client.analyzer;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.grpc.stats.controller.RecommendationsControllerGrpc;
import ru.practicum.ewm.grpc.stats.message.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.message.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.message.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.message.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendationsClient {

    @GrpcClient("analyzer")
    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            log.info("Запрос похожих событий для eventId={}, userId={}", eventId, userId);
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                    .toList();
        } catch (StatusRuntimeException exception) {
            log.error("Ошибка gRPC при запросе похожих событий: {}, статус: {}",
                    exception.getLocalizedMessage(), exception.getStatus().getCode());
            return Collections.emptyList();
        } catch (Exception exception) {
            log.error("Непредвиденная ошибка в RecommendationClient при запросе похожих событий: {}",
                    exception.getMessage());
            return Collections.emptyList();
        }
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        try {
            log.info("Запрос рекомендаций для user {}", userId);
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
        } catch (StatusRuntimeException exception) {
            log.error("Ошибка gRPC при запросе рекомендаций: {}, статус: {}",
                    exception.getLocalizedMessage(), exception.getStatus().getCode());
            return Stream.empty();
        } catch (Exception exception) {
            log.error("Непредвиденная ошибка в RecommendationClient при запросе рекомендаций: {}", exception.getMessage());
            return Stream.empty();
        }

    }

    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();
            Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                    .collect(Collectors.toMap(
                            RecommendedEventProto::getEventId,
                            RecommendedEventProto::getScore
                    ));
        } catch (StatusRuntimeException exception) {
            log.error("Ошибка gRPC при запросе мероприятий (GetInteractionsCount): {}, статус: {}",
                    exception.getLocalizedMessage(), exception.getStatus().getCode());
            return Collections.emptyMap();
        } catch (Exception exception) {
            log.error("Непредвиденная ошибка в RecommendationClient при запросе мероприятий (GetInteractionsCount): {}",
                    exception.getMessage());
            return Collections.emptyMap();
        }

    }


}
