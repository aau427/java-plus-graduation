package teamfive.repository;

import java.util.HashMap;
import java.util.Map;

public class MinWeightsSum {

    /*
         Сумма минимальных весов для каждой пары мероприятий.
         Ключ  - одно из мероприятий,
         значением — ещё одно отображение,
              ключ — второе мероприятие,
              значение — сумма их минимальных весов
     */
    private final Map<Long, Map<Long, Double>> minWeightsSums;

    public MinWeightsSum() {
        this.minWeightsSums = new HashMap<>();
    }

    public void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    public double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }
}
