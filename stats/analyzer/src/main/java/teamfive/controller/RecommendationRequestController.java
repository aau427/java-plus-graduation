package teamfive.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.grpc.stats.controller.RecommendationsControllerGrpc;
import ru.practicum.ewm.grpc.stats.message.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.message.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.message.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.message.UserPredictionsRequestProto;
import teamfive.service.RecommendationService;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RecommendationRequestController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService service;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            service.getRecommendationsForUser(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            log.error("Ошибка при получении рекомендаций для USER = {} : {}", request.getUserId(), exception.getMessage());
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(exception.getLocalizedMessage()).withCause(exception)));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            service.getSimilarEvents(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            log.error("Ошибка при получении похожих мероприятий. Event = {}, USER = {}. {} ",
                    request.getEventId(),
                    request.getUserId(),
                    exception.getMessage());
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(exception.getLocalizedMessage()).withCause(exception)));
        }

    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            service.getInteractionCount(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            log.error("Ошибка при получении мероприятий {} ", exception.getMessage());
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(exception.getLocalizedMessage()).withCause(exception)));
        }
    }
}
