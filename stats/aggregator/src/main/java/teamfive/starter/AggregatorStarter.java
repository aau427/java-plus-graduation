package teamfive.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import teamfive.producer.AggregatorKafkaClient;
import teamfive.service.AggregatorService;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AggregatorStarter {
    private final KafkaConsumer<Long, UserActionAvro> consumer;
    private final AggregatorKafkaClient kafkaClient;
    private final AggregatorService service;

    @Value("${spring.kafka.userevent.topic}")
    private String topicForConsumer;

    @Value("${spring.kafka.similarity.topic}")
    private String topicForProducer;

    public void start() {
        /* Регистрируем хук, в котором при завершении приложения
        будет вызван метод wakeup.
        Это приведёт к генерации WakeupException в методе poll.
        После этого работа консьюмера завершится. */
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(topicForConsumer));
            log.info("Агрегатор: Успешно подписались на топик {}", topicForConsumer);
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records =
                        consumer.poll(Duration.ofMillis(1000));
                log.debug("Агрегатор: получено {} сообщений", records.count());
                if (!records.isEmpty()) {
                    for (ConsumerRecord<Long, UserActionAvro> record : records) {
                        log.info("Агрегатор: Получено событие  {} для пользователя {}",
                                record.value().getEventId(),
                                record.value().getUserId());
                        List<EventSimilarityAvro> eventSimilarityList = service.processEvent(record.value());
                        eventSimilarityList.forEach(this::sendEvent);

                        // Фиксируем оффсет КОНКРЕТНОЙ записи
                        // Коммитим оффсет следующей записи (record.offset() + 1)
                        TopicPartition partition = new TopicPartition(record.topic(), record.partition());
                        OffsetAndMetadata offsetMetadata = new OffsetAndMetadata(record.offset() + 1);

                        consumer.commitSync(Collections.singletonMap(partition, offsetMetadata));
                    }
                }
            }
        } catch (WakeupException wakeupException) {
            log.warn("Поймали WakeupException, будет закругляться!");
        } catch (Exception e) {
            log.error("Ошибка при обработке события, будем закругляться!");
        } finally {
            consumer.close();
            //Spring сам закроет kafkaClient, так как он AutoCloseable.
        }
    }

    private void sendEvent(EventSimilarityAvro event) {
        log.info("Агрегатор: отправляю  {} в топик {}",
                event.getEventA(),
                topicForProducer);
        Long key = event.getEventA();
        long now = System.currentTimeMillis();
        kafkaClient.sendEvent(topicForProducer,
                key,
                event,
                now);
    }
}

