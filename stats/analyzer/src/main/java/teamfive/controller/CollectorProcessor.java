package teamfive.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import teamfive.handler.UserActionsHandler;

@Component
@Slf4j
public class CollectorProcessor extends BaseKafkaProcessor<Long, UserActionAvro> {
    private final UserActionsHandler handler;

    public CollectorProcessor(@Qualifier("getCollectorConsumer") KafkaConsumer<Long, UserActionAvro> consumer,
                              UserActionsHandler handler,
                              @Value("${kafka.topic.collector}") String topic) {
        super(consumer, topic);
        this.handler = handler;
    }


    @Override
    protected void process(UserActionAvro event) {
        handler.handle(event);
    }
}