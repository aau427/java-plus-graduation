package teamfive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamfive.model.Interaction;

import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);
}
