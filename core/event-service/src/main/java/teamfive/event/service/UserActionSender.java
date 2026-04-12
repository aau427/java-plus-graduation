package teamfive.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import teamfive.client.collector.CollectorClient;

@Component
@RequiredArgsConstructor
public class UserActionSender {

    private final CollectorClient collectorClient;

    public void sendView(Long userId, Long eventId) {
        collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }


    public void sendLike(Long userId, Long eventId) {
        collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }
}
