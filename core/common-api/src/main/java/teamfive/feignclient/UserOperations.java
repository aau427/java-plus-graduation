package teamfive.feignclient;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import teamfive.dto.UserDto;

import java.util.List;

public interface UserOperations {
    @GetMapping
    List<UserDto> get(@RequestParam(required = false) List<Long> ids,
                      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                      @RequestParam(defaultValue = "10") @Positive Integer size);
}
