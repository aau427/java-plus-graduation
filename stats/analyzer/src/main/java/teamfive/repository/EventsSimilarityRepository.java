package teamfive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamfive.model.EventProjection;
import teamfive.model.EventsSimilarity;

import java.util.List;
import java.util.Optional;

public interface EventsSimilarityRepository extends JpaRepository<EventsSimilarity, Long> {
    Optional<EventsSimilarity> findByEventAAndEventB(long eventA, long eventB);

    @Query(nativeQuery = true,
            value = """
                    SELECT 
                        CASE 
                            WHEN s.event_a = :targetEventId THEN s.event_b 
                            ELSE s.event_a 
                        END AS eventId, 
                        s.score AS totalScore
                    FROM events_similarity s
                    WHERE (s.event_a = :targetEventId OR s.event_b = :targetEventId)
                      AND (
                          CASE 
                              WHEN s.event_a = :targetEventId THEN s.event_b 
                              ELSE s.event_a 
                          END
                      ) NOT IN (
                          SELECT i.event_id 
                          FROM interactions i 
                          WHERE i.user_id = :userId
                      )
                    ORDER BY s.score DESC
                    LIMIT :limit
                    """)
    List<EventProjection> findSimilarByPrecomputedScore(
            @Param("targetEventId") long targetEventId,
            @Param("userId") long userId,
            @Param("limit") int limit
    );

    @Query(nativeQuery = true,
            value = """
                    SELECT 
                        CASE 
                            WHEN s.event_a = i.event_id THEN s.event_b 
                            ELSE s.event_a 
                        END AS eventId, 
                        s.score AS totalScore
                    FROM interactions i
                    INNER JOIN events_similarity s ON (s.event_a = i.event_id OR s.event_b = i.event_id)
                    WHERE i.user_id = :userId
                      AND NOT EXISTS (
                          SELECT 1 FROM interactions m 
                          WHERE m.user_id = :userId 
                            AND m.event_id = (CASE WHEN s.event_a = i.event_id THEN s.event_b ELSE s.event_a END)
                      )
                    ORDER BY totalScore DESC
                    LIMIT :limit
                    """)
    List<EventProjection> findEverySimilar(@Param("userId") long userId, @Param("limit") int limit);

    @Query(nativeQuery = true,
            value = """
                    SELECT 
                        eventId,
                        SUM(score * rating) / NULLIF(SUM(score), 0) AS totalScore
                    FROM (
                        SELECT 
                            t.target_id AS eventId,
                            s.score,
                            i.rating,
                            ROW_NUMBER() OVER (
                                PARTITION BY t.target_id 
                                ORDER BY s.score DESC
                            ) as neighbor_rank
                        FROM (SELECT unnest(CAST(:targetIds AS bigint[])) as target_id) AS t
                        JOIN events_similarity s ON (s.event_a = t.target_id OR s.event_b = t.target_id)
                        JOIN interactions i ON i.user_id = :userId 
                            AND i.event_id = (CASE WHEN s.event_a = t.target_id THEN s.event_b ELSE s.event_a END)
                    ) AS ranked_neighbors
                    WHERE neighbor_rank <= :k
                    GROUP BY eventId
                    ORDER BY totalScore DESC -- Сортируем от лучших предсказаний к худшим
                    """)
    List<EventProjection> predictScoresForList(
            @Param("userId") long userId,
            @Param("targetIds") Long[] targetIds,
            @Param("k") int k
    );


}
