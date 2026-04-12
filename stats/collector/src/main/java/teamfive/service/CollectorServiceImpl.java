package teamfive.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;
import teamfive.mapper.UserActionMapper;
import teamfive.producer.CollectorKafkaClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectorServiceImpl implements CollectorService {

    private final UserActionMapper userActionMapper;
    private final CollectorKafkaClient kafkaClient;

    @Value("${topic.collector.userevent.out}") // Обновленный ключ
    private String topic;

    @Override
    public void processUserAction(UserActionProto request) {
        UserActionAvro userActionAvro = userActionMapper.toAvro(request);

        log.info("Отправка действия пользователя в Kafka: user={}, event={}",
                userActionAvro.getUserId(), userActionAvro.getEventId());

        // 3. Отправляем через KafkaClient
        kafkaClient.sendEvent(
                topic,
                userActionAvro.getUserId(), // Ключ для партиционирования
                userActionAvro,
                userActionAvro.getTimestamp().toEpochMilli() // Передаем время события в мс
        );

    }
}
