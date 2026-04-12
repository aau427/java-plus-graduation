package teamfive.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.grpc.stats.message.RecommendedEventProto;
import teamfive.model.EventProjection;

@Mapper(componentModel = "spring")
public interface InteractionMapper {
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "score", source = "totalScore")
    RecommendedEventProto mapProjectionToProto(EventProjection projection);
}
