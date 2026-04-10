package teamfive.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.grpc.stats.message.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import teamfive.category.model.Category;
import teamfive.category.storage.CategoryRepository;
import teamfive.client.analyzer.RecommendationsClient;
import teamfive.client.collector.CollectorClient;
import teamfive.dto.event.EventInternalDto;
import teamfive.dto.event.EventResponseDto;
import teamfive.dto.event.EventShortDto;
import teamfive.dto.event.EventUpdateRequestDto;
import teamfive.dto.user.UserDto;
import teamfive.enums.EventState;
import teamfive.event.mapper.EventMapper;
import teamfive.event.model.Event;
import teamfive.event.model.EventLocation;
import teamfive.event.storage.EventRepository;
import teamfive.event.view.EventInternalView;
import teamfive.exception.ConflictException;
import teamfive.exception.NotFoundException;
import teamfive.exception.ValidationException;
import teamfive.feignclient.user.UserServiceClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final UserServiceClient userClient;
    private final CollectorClient collectorClient;
    private final RecommendationsClient recommendationsClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public List<EventResponseDto> getEventsByAdmin(List<Long> users, List<String> states,
                                                   List<Long> categories, String rangeStart,
                                                   String rangeEnd, int from, int size) {
        log.info("Поиск событий администратором: users={}, states={}, categories={}", users, states, categories);

        validatePaginationParams(from, size);
        int page = calculatePageNumber(from, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<Event> spec = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("initiatorId").in(users));
        }

        if (states != null && !states.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("state").as(String.class).in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("category").get("id").in(categories));
        }

        if (rangeStart != null) {
            LocalDateTime start = LocalDateTime.parse(rangeStart, FORMATTER);
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("eventDate"), start));
        }

        if (rangeEnd != null) {
            LocalDateTime end = LocalDateTime.parse(rangeEnd, FORMATTER);
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("eventDate"), end));
        }

        Page<Event> events = eventRepository.findAll(spec, pageable);
        Map<Long, Double> ratingsMap = getRatings(events.getContent());
        Map<Long, UserDto> usersMap = getUsersMap(events.getContent());

        return events.getContent().stream()
                .map(event -> {
                    EventResponseDto responseDto = eventMapper.toEventResponseDto(event);
                    responseDto.setRating(ratingsMap.getOrDefault(event.getId(), 0.0));
                    responseDto.setInitiator(usersMap.get(event.getId()));
                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventResponseDto updateEventByAdmin(Long eventId, EventUpdateRequestDto updateRequest) {
        log.info("Обновление события администратором: eventId={}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));


        if ("PUBLISH_EVENT".equals(updateRequest.getStateAction())) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Событие можно публиковать только в состоянии PENDING");
            }
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата начала события должна быть не ранее чем за час от публикации");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        if ("REJECT_EVENT".equals(updateRequest.getStateAction())) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Нельзя отклонить опубликованное событие");
            }
            event.setState(EventState.CANCELED);
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(new EventLocation(
                    updateRequest.getLocation().getLat(),
                    updateRequest.getLocation().getLon()
            ));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventResponseDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByPublic(String text, List<Long> categories,
                                                 Boolean paid, String rangeStart,
                                                 String rangeEnd, Boolean onlyAvailable,
                                                 String sort, int from, int size) {
        log.info("Поиск событий публичный: text={}, categories={}, paid={}", text, categories, paid);


        validatePaginationParams(from, size);

        Pageable pageable = createPageable(sort, from, size);

        Specification<Event> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("state"), EventState.PUBLISHED));


        if (categories != null && !categories.isEmpty()) {
            boolean anyCategoryExists = false;
            for (Long categoryId : categories) {
                if (categoryRepository.existsById(categoryId)) {
                    anyCategoryExists = true;
                    break;
                }
            }
            if (!anyCategoryExists) {
                throw new ValidationException("Указанные категории не существуют");
            }
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("category").get("id").in(categories));
        }

        if (paid != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("paid"), paid));
        }

        LocalDateTime startDateTime = rangeStart != null ?
                LocalDateTime.parse(rangeStart.replace(' ', 'T'), FORMATTER) : LocalDateTime.now();
        spec = spec.and((root, query, cb) ->
                cb.greaterThan(root.get("eventDate"), startDateTime));

        if (rangeEnd != null) {
            LocalDateTime endDateTime = LocalDateTime.parse(rangeEnd.replace(' ', 'T'), FORMATTER);
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("eventDate"), endDateTime));
        }

        if (onlyAvailable != null && onlyAvailable) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.isNull(root.get("participantLimit")),
                            cb.equal(root.get("participantLimit"), 0),
                            cb.lessThan(root.get("confirmedRequests"), root.get("participantLimit"))
                    ));
        }

        Page<Event> events = eventRepository.findAll(spec, pageable);

        Map<Long, UserDto> userDtoMap = getUsersMap(events.getContent());
        Map<Long, Double> ratingsMap = getRatings(events.getContent());

        return events.getContent().stream()
                .map(event -> {
                    EventShortDto eventShortDto = eventMapper.toEventShortDto(event);
                    eventShortDto.setRating(ratingsMap.getOrDefault(event.getId(), 0.0));
                    eventShortDto.setInitiator(userDtoMap.get(event.getInitiatorId()));
                    return eventShortDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDto getEventById(Long id) {
        log.info("Получение события по id: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + id + " не найдено");
        }

        Double rating = getRatings(List.of(event)).getOrDefault(event.getId(), 0.0);
        event.setRating(rating);
        EventResponseDto eventResponseDto = eventMapper.toEventResponseDto(event);
        UserDto userDto = userClient.getByIds(List.of(event.getInitiatorId()))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Не найден пользователь " + event.getInitiatorId()));
        eventResponseDto.setInitiator(userDto);

        return eventResponseDto;
    }

    @Override
    public EventInternalDto getEventInternalById(Long id) {
        log.info("Получение внутренней информации о событии id={}", id);

        EventInternalView view = eventRepository.findProjectedById(id)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        return eventMapper.toInternalDto(view);
    }

    @Override
    @Transactional
    public void incrementConfirmedRequests(Long eventId, Integer count) {
        log.info("Internal update: увеличение подтвержденных заявок события {} на {}", eventId, count);

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        eventRepository.incrementConfirmedRequests(eventId, count);
    }

    @Override
    public List<EventShortDto> getEventsRecommendations(Long userId, int maxResults) {
        List<Long> ids = recommendationsClient.getRecommendationsForUser(userId, maxResults)
                .map(RecommendedEventProto::getEventId)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return eventRepository.findByIdIn(ids)
                .stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public void sendView(Long userId, Long eventId) {
        collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    @Override
    public void sendLike(Long userId, Long eventId) {
        collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }


    private Pageable createPageable(String sort, int from, int size) {
        validatePaginationParams(from, size);

        Sort sorting = Sort.unsorted();

        if ("EVENT_DATE".equals(sort)) {
            sorting = Sort.by("eventDate").descending();
        } else if ("VIEWS".equals(sort)) {
            sorting = Sort.by("views").descending();
        }

        int page = calculatePageNumber(from, size);
        return PageRequest.of(page, size, sorting);
    }

    private void validatePaginationParams(int from, int size) {
        if (size <= 0) {
            throw new java.lang.IllegalArgumentException("Size must be positive");
        }
        if (from < 0) {
            throw new java.lang.IllegalArgumentException("From must be non-negative");
        }
    }

    private int calculatePageNumber(int from, int size) {
        return from / size;
    }


    private Map<Long, UserDto> getUsersMap(List<Event> eventList) {
        List<Long> userIdList = eventList
                .stream()
                .map(Event::getInitiatorId)
                .toList();
        List<UserDto> userDtoList = userClient.getByIds(userIdList);

        return userDtoList.stream()
                .collect(Collectors.toMap(UserDto::getId, userDto -> userDto));
    }

    private Map<Long, Double> getRatings(List<Event> eventList) {
        List<Long> ids = eventList.stream()
                .map(Event::getId)
                .toList();
        return recommendationsClient.getInteractionsCount(ids);
    }
}
