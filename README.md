## ExploreWithMe

Назначение: Управление событиями, пользователями, запросами на участие, комментариями и статистикой

## Архитектура

### Выделенные микросервисы:

1. **stats-server**:
    - хранит и предоставляет статистику по просмотрам событий
    - порт: динамический

2. **comment-service**:
    - управляет комментариями к событиям: создание, обновление, удаление, получение списков
    - порт: динамический

3. **event-service**:
    - управляет событиями: создание, обновление, публикация, получение списков (админ, приватный, публичный апи)
    - категории: admin api (создание, обновление, изменение), публичный api (получение инфо категория/список категорий)
    - подборки: admin api (создание,обновление, изменение), публичный api (получение инфо подборка/список подборок)
    - порт: динамически

4. **request-service**:
    - обрабатывает запросы на участие в событиях: создание, отмена, подтверждение/отклонение
    - порт: динамический

5. **user-service**:
    - управляет пользователями: создание, удаление, получение списков
    - порт: динамический

### Инфраструктурные компоненты

- **discovery-server** (реализован на базе Eureka Server): обнаружение сервисов, все микросервисы регистрируются здесь
  для динамического обнаружения
    - Порт: 8761.
- **config-server**: централизованное управление конфигурациями, загружает application.yaml для каждого сервиса из
  локальных файлов, расположенных по адресам:
  - classpath:config/core/{application}
  - classpath:config/stats/{application}
  - classpath:config/infra/{application}
    - Порт: динамический.
- **gateway-server** (Spring Cloud Gateway): шлюз для маршрутизации внешних запросов к сервисам, использует Eureka для
  маршрутизации по именам сервисов
    - Порт: 8080.
    - Маршруты: определяют пути для каждого сервиса (например, `/admin/events/**` → event-service)

### Взаимодействие между сервисами

- **обнаружение сервисов**: все сервисы регистрируются в Eureka
- **внутреннее взаимодействие**: через Feign-клиенты:
    - event-service → user-service (получение пользователей), stats-server (статистика)
    - comment-service → event-service (проверка событий), user-service (пользователи)
    - request-service → event-service (проверка событий), user-service (пользователи)
    - все сервисы → stats-server (для хитов просмотров)
- **база данных**: каждый сервис имеет свою бд
- **конфигурация**: через Config Server, каждый сервис импортирует конфиг с `spring.config.import=configserver:`

## Внутренний API для взаимодействия сервисов

Внутренний апи реализован через Feign-интерфейсы, расположенные в модуле *
*core/commom-api/src/main/java/teamfive/feignclient/**
а также через stats-client, расположенный в модуле **stats/stats-client**

1. **EventServiceClient**
    - увеличить количество подтвержденных запросов для события:
      `void incrementConfirmedRequests(
               @PathVariable("eventId") Long eventId,
               @RequestParam("count") Integer count`
    - : получить событие по Id:
      `EventInternalDto getEventInternalById(@PathVariable Long eventId);`

2**UserClient**
- получить инициаторов по списку ID:
`List<UserDto> getByIds(List<Long> ids)`

3**StatsClient** (stats-server):
- добавить хит (просмотр):
`public void hit(HttpServletRequest request)`
- получить статистику:
`public List<StatDto> getStats(ParamRequest paramRequest)`

## Внешний API

Внешний апи доступен через шлюз gateway-server на порту 8080
спецификации:
[Все сервисы, за исключением сервиса статистики](https://github.com/aau427/java-plus-graduation/blob/main/ewm-main-service-spec.json)
[Сервис статистики](https://github.com/aau427/java-plus-graduation/blob/main/ewm-stats-service-spec.json)

Маршруты определены в application.yaml сервиса gateway-server.
Внимание: реальный application.yaml сервиса выдает config-server (см. выше)

