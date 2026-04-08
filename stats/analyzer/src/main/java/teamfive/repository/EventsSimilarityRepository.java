package teamfive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamfive.model.EventsSimilarity;

import java.util.Optional;

public interface EventsSimilarityRepository extends JpaRepository<EventsSimilarity, Long> {
    Optional<EventsSimilarity> findByEventAAndEventB(long eventA, long eventB);
}
