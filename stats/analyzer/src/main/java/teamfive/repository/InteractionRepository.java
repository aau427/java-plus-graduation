package teamfive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamfive.model.EventProjection;
import teamfive.model.Interaction;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("""
            SELECT i.eventId as eventId, COALESCE(SUM(i.rating),0) as totalScore
            FROM Interaction i
            WHERE i.eventId IN :eventIds
            GROUP BY i.eventId
            """)
    List<EventProjection> getSumRatingsByEventIds(@Param("eventIds") List<Long> eventIds);
}
