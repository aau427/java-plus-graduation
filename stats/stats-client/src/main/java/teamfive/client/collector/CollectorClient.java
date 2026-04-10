package teamfive.client.collector;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class CollectorClient {

    @GrpcClient("collector")
    private final UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void collectUserAction(long userId, long eventId, ActionTypeProto actionType) {
        try {
            UserActionProto request = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(actionType)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .build())
                    .build();

            log.info("Отправка действия в коллектор: user={}, event={}, type={}",
                    userId, eventId, actionType);

            client.collectUserAction(request);

        } catch (StatusRuntimeException exception) {
            log.error("Ошибка gRPC при обращении к коллектору: {}, статус: {}",
                    exception.getLocalizedMessage(), exception.getStatus().getCode());
        } catch (Exception exception) {
            log.error("Непредвиденная ошибка в CollectorClient: {}", exception.getMessage());
        }
    }
}
