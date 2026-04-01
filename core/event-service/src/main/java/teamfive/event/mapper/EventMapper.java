package teamfive.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import teamfive.dto.event.EventInternalDto;
import teamfive.dto.event.EventResponseDto;
import teamfive.dto.event.EventShortDto;
import teamfive.enums.EventState;
import teamfive.event.model.Event;
import teamfive.event.view.EventInternalView;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "state", source = "state", qualifiedByName = "stateToString")
    EventResponseDto toEventResponseDto(Event event);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", ignore = true)
    EventShortDto toEventShortDto(Event event);

    EventInternalDto toInternalDto(EventInternalView view);

    @Named("stateToString")
    default String stateToString(EventState state) {
        return state != null ? state.name() : null;
    }
}
