package teamfive.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.List;

@Slf4j
public abstract class BaseKafkaProcessor<K, T> implements Runnable {
    private final KafkaConsumer<K, T> consumer;
    private final String topic;

    protected BaseKafkaProcessor(KafkaConsumer<K, T> consumer, String topic) {
        this.consumer = consumer;
        this.topic = topic;
    }

    protected abstract void process(T value);

    @Override
    public void run() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(List.of(topic));
            log.info("Analyzer: подписался на топик: {}", topic);

            while (true) {
                ConsumerRecords<K, T> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<K, T> record : records) {
                    process(record.value());
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        } catch (WakeupException exception) {
            log.info("Analyzer: {} останавливается", this.getClass().getSimpleName());
        } catch (Exception exception) {
            log.error("Analyzer: Критическая ошибка в {} : {}", this.getClass().getSimpleName(), exception.getMessage());
        } finally {
            consumer.close(Duration.ofSeconds(1));
        }
    }
}
