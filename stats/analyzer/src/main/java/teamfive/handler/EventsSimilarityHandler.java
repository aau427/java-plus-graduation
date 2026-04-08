package teamfive.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import teamfive.model.EventsSimilarity;
import teamfive.repository.EventsSimilarityRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class EventsSimilarityHandler {
    private final EventsSimilarityRepository repository;

    public void handle(EventSimilarityAvro event) {
        long first = Math.min(event.getEventA(), event.getEventB());
        long second = Math.max(event.getEventA(), event.getEventB());
        // У косинусного сходства мы просто сохраняем последний актуальный расчет
        repository.findByEventAAndEventB(first, second)
                .ifPresentOrElse(
                        existing -> {
                            if(event.getTimestamp().isAfter(existing.getTimestamp())) {
                                existing.setScore(event.getScore());
                                existing.setTimestamp(event.getTimestamp());
                                repository.save(existing);
                            }
                        },
                        () -> {
                            EventsSimilarity similarity = new EventsSimilarity();
                            similarity.setEventA(first);
                            similarity.setEventB(second);
                            similarity.setScore(event.getScore());
                            similarity.setTimestamp(event.getTimestamp());
                            repository.save(similarity);
                        }
                );
    }
}
