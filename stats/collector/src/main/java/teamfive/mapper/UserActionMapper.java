package teamfive.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface UserActionMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "actionType", source = "actionType", qualifiedByName = "mapActionType")
    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "mapTimestamp")
    UserActionAvro toAvro(UserActionProto proto);

    @Named("mapActionType")
    default ActionTypeAvro mapActionType(ActionTypeProto protoType) {
        if (protoType == null) return ActionTypeAvro.VIEW;
        return switch (protoType) {
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> ActionTypeAvro.VIEW;
        };
    }

    @Named("mapTimestamp")
    default Instant mapTimestamp(com.google.protobuf.Timestamp protoTimestamp) {
        if (protoTimestamp == null) return Instant.now();
        return Instant.ofEpochSecond(
                protoTimestamp.getSeconds(),
                protoTimestamp.getNanos()
        );
    }
}
