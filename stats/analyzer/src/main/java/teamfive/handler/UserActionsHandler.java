package teamfive.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import teamfive.model.Interaction;
import teamfive.repository.InteractionRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserActionsHandler {

    private final InteractionRepository repository;

    public void handle(UserActionAvro event) {
        Double newRating = getWeight(event.getActionType());

        repository.findByUserIdAndEventId(event.getUserId(), event.getEventId())
                .ifPresentOrElse(
                        existing -> {
                            //рейтинг может только расти!!!!!
                            if (newRating > existing.getRating()) {
                                existing.setRating(newRating);
                                existing.setTimestamp(event.getTimestamp());
                                repository.save(existing);
                            }
                        },
                        () -> {
                            // Если записи еще нет — просто создаем новую
                            Interaction interaction = new Interaction();
                            interaction.setUserId(event.getUserId());
                            interaction.setEventId(event.getEventId());
                            interaction.setRating(newRating);
                            interaction.setTimestamp(event.getTimestamp());
                            repository.save(interaction);
                        }
                );
    }

    private Double getWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
