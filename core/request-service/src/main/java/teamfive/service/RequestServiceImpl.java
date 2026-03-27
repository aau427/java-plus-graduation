package teamfive.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.dto.event.EventInternalDto;
import teamfive.dto.event.EventRequestStatusUpdateRequest;
import teamfive.dto.request.EventRequestStatusUpdateResult;
import teamfive.dto.request.ParticipationRequestDto;
import teamfive.dto.user.UserDto;
import teamfive.enums.EventState;
import teamfive.enums.RequestStatus;
import teamfive.exception.ConflictException;
import teamfive.exception.DuplicatedException;
import teamfive.exception.NotFoundException;
import teamfive.feignclient.EventServiceClient;
import teamfive.feignclient.UserServiceClient;
import teamfive.mapper.RequestMapper;
import teamfive.model.ParticipationRequest;
import teamfive.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository repository;
    private final UserServiceClient userClient;
    private final RequestMapper mapper;
    private final EventServiceClient eventClient;

    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.info("Создание запроса: userId={}, eventId={}", userId, eventId);

        if (repository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new DuplicatedException("Заявка на участие в этом событии уже создана");
        }

        EventInternalDto event = eventClient.getEventInternalById(eventId);

        validateParticipation(event, userId);

        int limit = Objects.requireNonNullElse(event.getParticipantLimit(), 0);
        boolean moderation = Objects.requireNonNullElse(event.getRequestModeration(), true);

        long confirmedCount = (limit > 0)
                ? repository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                : 0;

        if (limit > 0 && confirmedCount >= limit) {
            throw new ConflictException("Достигнут лимит участников события");
        }

        RequestStatus status = (limit == 0 || !moderation)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest savedRequest = repository.save(request);

        if (status == RequestStatus.CONFIRMED) {
            eventClient.incrementConfirmedRequests(eventId, 1);
            log.info("Счетчик участников события {} увеличен на 1", eventId);
        }

        log.info("Заявка успешно создана с ID={} и статусом {}", savedRequest.getId(), status);
        return mapper.toDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {
        try {
            log.info("Получение заявок пользователя: userId={}", userId);

            UserDto user = userClient.getByIds(List.of(userId))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

            if (user == null) {
                throw new NotFoundException("Пользователь с id=" + userId + " не найден");
            }

            List<ParticipationRequest> requests = repository.findAllByRequesterId(userId);
            log.info("Найдено {} заявок для пользователя {}", requests.size(), userId);

            return requests.stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении заявок пользователя {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера при получении заявок");
        }
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        try {
            ParticipationRequest request = repository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

            log.info("Найдена заявка: requesterId={}, status={}", request.getRequesterId(), request.getStatus());

            if (!request.getRequesterId().equals(userId)) {
                log.error("Попытка отменить чужую заявку: userId={}, заявка принадлежит userId={}",
                        userId, request.getRequesterId());
                throw new ConflictException("Пользователь, который не является автором заявки, не может её отменить.");
            }

            RequestStatus currentStatus = request.getStatus();
            if (RequestStatus.CANCELED.equals(currentStatus)) {
                log.info("Заявка {} уже отменена", requestId);
                return mapper.toDtoSafe(request);
            }

            log.info("Статус заявки изменен: requestId={}, oldStatus={}, newStatus=CANCELED",
                    requestId, currentStatus);

            request.setStatus(RequestStatus.CANCELED);
            ParticipationRequest updatedRequest = repository.save(request);
            log.info("Статус заявки изменен: requestId={}, oldStatus={}, newStatus=CANCELED",
                    requestId, currentStatus);

            ParticipationRequestDto result = mapper.toDtoSafe(updatedRequest);
            if (result == null) {
                throw new RuntimeException("Ошибка преобразования данных отмененной заявки");
            }

            log.info("Участие в событии для пользователя с id={} отменено", userId);
            return result;

        } catch (NotFoundException | ConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при отмене заявки {}: {}", requestId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера при отмене заявки");
        }
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId) {
        log.info("Получение запросов на участие для события: userId={}, eventId={}", userId, eventId);
        try {
            List<ParticipationRequest> requests = repository.findAllByEventId(eventId);
            log.debug("Найдено запросов для события {}: {}", eventId, requests.size());
            return requests.stream()
                    .map(mapper::toDtoSafe)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (DataAccessException e) {
            log.error("Ошибка доступа к данным при получении запросов для события {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Ошибка базы данных при получении запросов события");
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении запросов для события {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера при получении запросов события");
        }
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId,
                                                                EventRequestStatusUpdateRequest updateRequest) {
        log.info("Обновление статусов запросов: userId={}, eventId={}, requestIds={}, status={}",
                userId, eventId, updateRequest.getRequestIds(), updateRequest.getStatus());

        EventInternalDto event = eventClient.getEventInternalById(eventId);
        if (event.getInitiatorId() == null || !event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Только инициатор события может обновлять запросы на участие");
        }

        int currentConfirmed = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0;
        int participantLimit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;

        if (RequestStatus.CONFIRMED.equals(updateRequest.getStatus()) && participantLimit > 0) {
            int requestsToConfirm = updateRequest.getRequestIds().size();

            if (currentConfirmed + requestsToConfirm > participantLimit) {
                throw new ConflictException("Достигнут лимит участников события");
            }
        }

        List<ParticipationRequest> requestsToUpdate = repository.findAllByIdIn(updateRequest.getRequestIds());

        if (requestsToUpdate.size() != updateRequest.getRequestIds().size()) {
            throw new NotFoundException("Некоторые запросы не найдены");
        }


        requestsToUpdate.forEach(request -> {
            if (!request.getEventId().equals(eventId)) {
                throw new ConflictException("Запрос не принадлежит указанному событию");
            }
            if (!RequestStatus.PENDING.equals(request.getStatus())) {
                throw new ConflictException("Можно изменять только запросы в статусе PENDING");
            }
            request.setStatus(updateRequest.getStatus());
        });

        int newlyConfirmed = RequestStatus.CONFIRMED.equals(updateRequest.getStatus())
                ? requestsToUpdate.size()
                : 0;

        List<ParticipationRequest> updatedRequests = repository.saveAll(requestsToUpdate);
        repository.flush();

        if (newlyConfirmed > 0) {
            eventClient.incrementConfirmedRequests(eventId, newlyConfirmed);
        }

        log.info("Статусы запросов успешно обновлены");

        List<ParticipationRequestDto> updatedDtos = updatedRequests.stream()
                .map(mapper::toDto)
                .toList();

        Map<RequestStatus, List<ParticipationRequestDto>> groupedRequests = updatedDtos.stream()
                .collect(Collectors.groupingBy(ParticipationRequestDto::getStatus));

        List<ParticipationRequestDto> confirmedRequests = groupedRequests.getOrDefault(RequestStatus.CONFIRMED, List.of());
        List<ParticipationRequestDto> rejectedRequests = groupedRequests.getOrDefault(RequestStatus.REJECTED, List.of());

        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    private void validateParticipation(EventInternalDto event, Long userId) {
        if (event.getInitiatorId() == null || event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Инициатор не может участвовать в своем событии");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
    }
}

