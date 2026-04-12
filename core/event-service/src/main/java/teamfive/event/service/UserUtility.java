package teamfive.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import teamfive.dto.user.UserDto;
import teamfive.event.model.Event;
import teamfive.exception.NotFoundException;
import teamfive.feignclient.user.UserServiceClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserUtility {

    private final UserServiceClient userClient;

    public Map<Long, UserDto> getUsersMap(List<Event> eventList) {
        List<Long> userIdList = eventList
                .stream()
                .map(Event::getInitiatorId)
                .toList();
        List<UserDto> userDtoList = userClient.getByIds(userIdList);

        return userDtoList.stream()
                .collect(Collectors.toMap(UserDto::getId, userDto -> userDto));
    }

    public UserDto getUserDto(Long userId) {
        return userClient.getByIds(List.of(userId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Не найден пользователь " + userId));
    }

}
