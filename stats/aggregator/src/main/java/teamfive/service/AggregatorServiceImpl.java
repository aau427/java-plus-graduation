package teamfive.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import teamfive.repository.AggregatorRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {
    private final AggregatorRepository repository;

    @Override
    public List<EventSimilarityAvro> processEvent(UserActionAvro event) {
        long eventA = event.getEventId();
        long userId = event.getUserId();
        double newWeight = getWeight(event);
        //если мероприятие абсолютно новое для системы (никто не взаимодействовал с ним)
        if (!repository.hasEvent(eventA)) {
            //Ранее НИКТО из пользователей еще не взаимодействовал с этим мероприятием
            repository.updateUserWeight(eventA, userId, newWeight);
            repository.setTotalWeight(eventA, newWeight);
            AddMinWeightSumForAllPairs(eventA, newWeight, userId);
            return fullComputeAllSimilarity(event.getEventId(), event.getTimestamp());
        }

        double currentWeight = repository.getUserWeight(eventA, userId);
        //новый вес не больше старого, бессмысленно перерассчитывать, т.к. min весов не поменяется.
        if (newWeight <= currentWeight) {
            return Collections.emptyList();
        }
        List<EventSimilarityAvro> returnList = deltaComputeAndSave(eventA,
                userId,
                currentWeight,
                newWeight,
                event.getTimestamp());

        repository.updateUserWeight(eventA, userId, newWeight);
        repository.addDeltaToTotalWeight(eventA, newWeight - currentWeight);
        return returnList;
    }

    private double getWeight(UserActionAvro event) {
        ActionTypeAvro type = event.getActionType();
        return switch (type) {
            case VIEW -> ActionTypeConstants.VIEW;
            case REGISTER -> ActionTypeConstants.REGISTER;
            case LIKE -> ActionTypeConstants.LIKE;
        };
    }

    /*
            Суть, появилось действие над мероприятием А, причем пользователи ранее с ним не взаимодействовали
            т.е. нужно добавить "суммы минимальных весов" для события A и остальных
    */
    private void AddMinWeightSumForAllPairs(long eventA, double newWeight, long userId) {
        repository.getAllEventIds().forEach(
                eventB -> {
                    if (eventA == eventB) return;
                    double weightB = repository.getUserWeight(eventB, userId);
                    if (weightB > 0.0) {
                        double weighMin = Math.min(weightB, newWeight);
                        repository.updateMinWeightSum(eventA, eventB, weighMin);
                    }
                    //а если weightB == 0, то минимум 0, т.е. и не надо перерассчитывать.
                }
        );

    }

    private List<EventSimilarityAvro> fullComputeAllSimilarity(Long eventA, Instant instant) {
        /*
            Пройтись по всем возможным B и подсчитать косинус
         */
        List<EventSimilarityAvro> returnList = new ArrayList<>();
        repository.getAllEventIds().forEach(eventB -> {
            if (eventB.equals(eventA)) {
                return;
            }
            //сумма минимальных весов для пары (A, B)
            double nominator = repository.getMinWeightSum(eventA, eventB);
            if (nominator == 0.0) {
                return;
            }
            double denominator = Math.sqrt(repository.getTotalWeight(eventA)) * Math.sqrt(repository.getTotalWeight(eventB));
            //логически невозможно, чтобы знаменатель был равен 0, но фик знает...
            if (denominator == 0.0) {
                log.error("Внимание! Знаменатель равен 0 при расчете косинуса для события {}", eventA);
            }
            double score = (denominator > 0) ? nominator / denominator : 0.0;
            if (score > 0) returnList.add(getEventAvro(eventA, eventB, score, instant));
        });

        return returnList;
    }

    private EventSimilarityAvro getEventAvro(long eventA,
                                             long eventB,
                                             double score,
                                             Instant instant) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(score)
                .setTimestamp(instant)
                .build();
    }

    private List<EventSimilarityAvro> deltaComputeAndSave(long eventA,
                                                          long userId,
                                                          double currentWeight,
                                                          double newWeight,
                                                          Instant timestamp) {
        /*
            Имеем:  хотя бы один из пользователей ранее взаимодействовал
            с мероприятием, причем ВАЖНО!!! новый вес больше, чем предыдущий, следовательно,
            необходимо инкрементально перерассчитать близость А и этих событий попарно.
            Важно, если пользователь ранее не взаимодействовал с мероприятием B, то для
            этой пары близость не перерассчитываем.
         */

        List<EventSimilarityAvro> returnList = new ArrayList<>();
        //отобрать только те мероприятия, с которым взаимодействовал пользователь
        double futureMinWeightA = repository.getTotalWeight(eventA) + newWeight - currentWeight;
        repository.getAllEventIds().forEach(eventB -> {
            if (eventB == eventA) return;
            /*
               Рассмотрим пару мероприятий A и B. Если пользователь не взаимодействовал с мероприятием B,
               поэтому не вносил свой вес в коэффициент сходства этих мероприятий.
               Пересчитывать его нет смысла.
             */
            double weightB = repository.getUserWeight(eventB, userId);
            if (weightB == 0) {
                return;
            }

            double deltaSumMin = Math.min(weightB, newWeight) - Math.min(weightB, currentWeight);
            double oldSumMinWeighsAB = repository.getMinWeightSum(eventA, eventB);
            double nominator = oldSumMinWeighsAB + deltaSumMin;
            if (nominator == 0) {
                return;
            }

            repository.updateMinWeightSum(eventA, eventB, nominator);

            //В знаменателе нужно пересчитать только сумму весов события А
            double denominator = Math.sqrt(futureMinWeightA) * Math.sqrt(repository.getTotalWeight(eventB));
            if (denominator == 0.0) {
                log.error("Внимание! Знаменатель равен 0 при расчете косинуса для события {}", eventA);
            }
            double score = (denominator > 0) ? nominator / denominator : 0.0;
            if (score > 0) returnList.add(getEventAvro(eventA, eventB, score, timestamp));
        });
        return returnList;
    }
}
