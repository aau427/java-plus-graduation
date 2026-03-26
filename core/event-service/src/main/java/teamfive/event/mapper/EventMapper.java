package teamfive.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import teamfive.dto.event.EventInternalDto;
import teamfive.dto.event.EventResponseDto;
import teamfive.dto.event.EventShortDto;
import teamfive.dto.user.UserDto;
import teamfive.enums.EventState;
import teamfive.event.model.Event;
import teamfive.event.view.EventInternalView;
import teamfive.feignclient.UserServiceClient;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class EventMapper {
    @Lazy
    @Autowired
    protected UserServiceClient userClient;

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiatorId", qualifiedByName = "fetchUser")
    @Mapping(target = "state", source = "state", qualifiedByName = "stateToString")
    public abstract EventResponseDto toEventResponseDto(Event event);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiatorId", qualifiedByName = "fetchUser")
    public abstract EventShortDto toEventShortDto(Event event);

    public abstract EventInternalDto toInternalDto(EventInternalView view);


    @Named("fetchUser")
    protected UserDto fetchUser(Long initiatorId) {
        if (initiatorId == null) return null;
        List<UserDto> users = userClient.getByIds(List.of(initiatorId));
        return users.isEmpty() ? null : users.getFirst();
    }

    @Named("stateToString")
    protected String stateToString(EventState state) {
        return state != null ? state.name() : null;
    }
}
