package teamfive.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface AggregatorService {
    List<EventSimilarityAvro> processEvent(UserActionAvro event);
}
