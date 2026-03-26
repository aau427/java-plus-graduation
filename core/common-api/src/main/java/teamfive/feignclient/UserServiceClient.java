package teamfive.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import teamfive.dto.user.UserDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserServiceClient extends UserOperations {
    default List<UserDto> getByIds(List<Long> ids) {
        return get(ids, 0, 10);
    }
}
