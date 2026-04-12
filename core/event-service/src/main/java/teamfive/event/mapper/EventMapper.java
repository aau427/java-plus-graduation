package teamfive.event.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import teamfive.dto.event.EventInternalDto;
import teamfive.dto.event.EventResponseDto;
import teamfive.dto.event.EventShortDto;
import teamfive.dto.user.UserDto;
import teamfive.enums.EventState;
import teamfive.event.model.Event;
import teamfive.event.view.EventInternalView;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "initiator", expression = "java(usersMap != null ? usersMap.get(event.getInitiatorId()) : null)")
    @Mapping(target = "state", source = "event.state", qualifiedByName = "stateToString")
    EventResponseDto toEventResponseDto(Event event, @Context Map<Long, UserDto> usersMap);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "initiator", source = "userDto")
    @Mapping(target = "state", source = "event.state", qualifiedByName = "stateToString")
    EventResponseDto toEventResponseDto(Event event, UserDto userDto);

    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "initiator", expression = "java(usersMap != null ? usersMap.get(event.getInitiatorId()) : null)")
    EventShortDto toEventShortDto(Event event, @Context Map<Long, UserDto> usersMap);

    EventInternalDto toInternalDto(EventInternalView view);

    @Named("stateToString")
    default String stateToString(EventState state) {
        return state != null ? state.name() : null;
    }
}
