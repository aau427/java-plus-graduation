package teamfive.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.grpc.stats.message.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.message.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.message.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.message.UserPredictionsRequestProto;
import teamfive.mapper.InteractionMapper;
import teamfive.repository.EventsSimilarityRepository;
import teamfive.repository.InteractionRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final InteractionRepository interactionRepository;
    private final EventsSimilarityRepository similarityRepository;
    private final InteractionMapper mapper;

    // Считает из рекомендации.limit-interactions, если нет - возьмет 100
    @Value("${recommendations.limit-interactions:100}")
    private int limitInteractions;

    // Считает из рекомендации.nearest-neighbors, если нет - возьмет 100
    @Value("${recommendations.nearest-neighbors:100}")
    private int nearestNeighbors;

    /*
        возвращает список мероприятий с указанием с суммой максимальных весов
        действий каждого пользователя с этим мероприятием
     */
    @Override
    public Stream<RecommendedEventProto> getInteractionCount(InteractionsCountRequestProto request) {
        if (request.getEventIdList().isEmpty()) {
            log.info("В запросе на список мероприятий передан пустой лист их идентификаторов!");
            return Stream.empty();
        }
        return interactionRepository.getSumRatingsByEventIds(request.getEventIdList())
                .stream()
                .map(mapper::mapProjectionToProto);
    }

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        if (request.getMaxResults() == 0) {
            log.info("В запросе рекомендаций для пользователя {} указан MaxCount = 0 !", request.getUserId());
            return Collections.emptyList();
        }
        Long[] ids = similarityRepository
                .findEverySimilar(request.getUserId(), limitInteractions).toArray(Long[]::new);
        if (ids.length == 0) {
            log.info("Не нашел мероприятий, с которыми пользователь {} взаимодействовал. Нечего рекомендовать!",
                    request.getUserId());
            return Collections.emptyList();
        }

        return similarityRepository.predictScoresForList(request.getUserId(),
                        ids, nearestNeighbors,
                        request.getMaxResults())
                .stream()
                .map(mapper::mapProjectionToProto)
                .toList();
    }

    /*  возвращает список мероприятий, с которыми пользователь не взаимодействовал,
        максимально похожи на указанное мероприятие.
     */
    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        if (request.getMaxResults() == 0) {
            log.info("В запросе на список похожих мероприятий передан MaxCount = 0 !");
            return Stream.empty();
        }
        return similarityRepository.findSimilarByPrecomputedScore(request.getEventId(),
                        request.getUserId(),
                        request.getMaxResults())
                .stream()
                .map(mapper::mapProjectionToProto);
    }

}
