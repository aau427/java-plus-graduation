package teamfive.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import teamfive.dto.user.UserDto;
import teamfive.dto.user.UserRequestDto;
import teamfive.dto.user.UserUpdateDto;
import teamfive.feignclient.user.UserOperations;
import teamfive.service.UserService;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/users")
@Validated
public class UserController implements UserOperations {
    private final UserService userService;

    @Override
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> get(@RequestParam(required = false) List<Long> ids,
                             /* валидация параметров определена на уровне интерфейса
                                лично неизвестная мне Барбара Лисков за это топит активно:)
                              */
                             @RequestParam(defaultValue = "0") Integer from,
                             @RequestParam(defaultValue = "10") Integer size) {

        log.info("GET: Получение пользователей с параметрами: ids={}, from={}, size={}", ids, from, size);

        Sort sort = Sort.unsorted();
        PageRequest pageable = PageRequest.of(from, size, sort);

        log.debug("Создан объект PageRequest: {}", pageable);

        List<UserDto> result = userService.get(ids, pageable).toList();

        log.info("Возвращается {} пользователей", result.size());

        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto add(@Valid @RequestBody UserRequestDto user) {

        log.info("POST: Создание пользователя: {}", user);

        UserDto createdUser = userService.create(user);

        log.debug("Пользователь успешно создан");

        return createdUser;
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto patch(@PathVariable @Positive Long userId,
                         @Valid @RequestBody UserUpdateDto user) {

        log.info("PATCH: Обновление пользователя с ID={}", userId);

        user.setId(userId);

        UserDto patchedUser = userService.patch(user);

        log.info("Пользователь успешно обновлен: {}", patchedUser);

        return patchedUser;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        log.info("Получен запрос на удаление пользователя с ID={}", id);
        userService.delete(id);
        log.info("Пользователь с ID={} успешно удалён", id);
    }
}
