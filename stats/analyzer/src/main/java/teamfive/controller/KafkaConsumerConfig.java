package teamfive.controller;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import teamfive.EventSimilarityDeserializer;
import teamfive.UserAvroDeserializer;

import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.group-id.collector}")
    private String collectorGroupId;

    @Value("${kafka.group-id.aggregator}")
    private String aggregatorGroupId;

    @Value("${kafka.auto-commit}")
    private boolean autoCommit;

    // Консьюмер для CollectorProcessor
    @Bean
    public KafkaConsumer<Long, UserActionAvro> getCollectorConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, collectorGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserAvroDeserializer.class);

        return new KafkaConsumer<>(props);
    }

    // Консьюмер для AggregatorProcessing
    @Bean
    public KafkaConsumer<Long, EventSimilarityAvro> getAggregatorConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, aggregatorGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class);

        return new KafkaConsumer<>(props);
    }
}
