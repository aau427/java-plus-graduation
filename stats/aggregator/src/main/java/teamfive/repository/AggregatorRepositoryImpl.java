package teamfive.repository;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Repository
public class AggregatorRepositoryImpl implements AggregatorRepository {

    // Текущие максимальные веса действий пользователей (мероприятие -> (пользователь -> вес))
    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();

    // Общие суммы весов каждого мероприятия
    private final Map<Long, Double> totalEventWeights = new HashMap<>();

    // Сумма минимальных весов для каждой пары (числитель)
    private final MinWeightsSum minWeightsSum = new MinWeightsSum();

    @Override
    public double getUserWeight(long eventId, long userId) {
        return eventUserWeights.getOrDefault(eventId, Collections.emptyMap())
                .getOrDefault(userId, 0.0);
    }

    @Override
    public void updateUserWeight(long eventId, long userId, double newWeight) {
        eventUserWeights.computeIfAbsent(eventId, k -> new HashMap<>())
                .put(userId, newWeight);
    }

    @Override
    public Map<Long, Double> getUsersWeightsForEvent(long eventId) {
        return eventUserWeights.getOrDefault(eventId, Collections.emptyMap());
    }

    @Override
    public boolean hasEvent(long eventId) {
        return eventUserWeights.containsKey(eventId);
    }

    @Override
    public double getTotalWeight(long eventId) {
        return totalEventWeights.getOrDefault(eventId, 0.0);
    }

    @Override
    public void addDeltaToTotalWeight(long eventId, double delta) {
        totalEventWeights.merge(eventId, delta, Double::sum);
    }

    @Override
    public void setTotalWeight(long eventId, double weight) {
        totalEventWeights.put(eventId, weight);
    }

    @Override
    public Set<Long> getAllEventIds() {
        return totalEventWeights.keySet();
    }

    @Override
    public double getMinWeightSum(long eventA, long eventB) {
        return minWeightsSum.get(eventA, eventB);
    }

    @Override
    public void updateMinWeightSum(long eventA, long eventB, double newSum) {
        minWeightsSum.put(eventA, eventB, newSum);
    }
}
