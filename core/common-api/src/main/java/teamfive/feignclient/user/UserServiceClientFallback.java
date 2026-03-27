package teamfive.feignclient.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import teamfive.dto.user.UserDto;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {
    @Override
    public List<UserDto> get(List<Long> ids, Integer from, Integer size) {
        log.error("[Fallback] User-service недоступен. Возвращаем пустой список. А какие варианты?");
        return Collections.emptyList();
    }
}