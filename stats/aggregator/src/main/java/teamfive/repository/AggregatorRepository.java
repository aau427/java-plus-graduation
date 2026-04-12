package teamfive.repository;

import java.util.Map;
import java.util.Set;

public interface AggregatorRepository {

    // Работа с весами конкретных пользователей (eventUserWeights)
    double getUserWeight(long eventId, long userId);

    void updateUserWeight(long eventId, long userId, double newWeight);

    Map<Long, Double> getUsersWeightsForEvent(long eventId);

    boolean hasEvent(long eventId);

    // Работа с общими суммами весов мероприятий
    double getTotalWeight(long eventId);

    void addDeltaToTotalWeight(long eventId, double delta);

    void setTotalWeight(long eventId, double weight);

    Set<Long> getAllEventIds();

    // Работа с суммами минимальных весов пар (minWeightsSum - числитель)
    double getMinWeightSum(long eventA, long eventB);

    void updateMinWeightSum(long eventA, long eventB, double newSum);
}
