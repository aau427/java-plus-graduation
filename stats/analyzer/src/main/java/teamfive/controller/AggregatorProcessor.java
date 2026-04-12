package teamfive.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import teamfive.handler.EventsSimilarityHandler;

@Component
@Slf4j
public class AggregatorProcessor extends BaseKafkaProcessor<Long, EventSimilarityAvro> {

    private final EventsSimilarityHandler handler;

    public AggregatorProcessor(@Qualifier("getAggregatorConsumer") KafkaConsumer<Long, EventSimilarityAvro> consumer,
                               EventsSimilarityHandler handler,
                               @Value("${kafka.topic.aggregator}") String topic) {
        super(consumer, topic);
        this.handler = handler;
    }


    @Override
    protected void process(EventSimilarityAvro event) {
        handler.handle(event);
    }
}
