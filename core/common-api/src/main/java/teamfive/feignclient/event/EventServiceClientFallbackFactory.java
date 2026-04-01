package teamfive.feignclient.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import teamfive.dto.event.EventInternalDto;
import teamfive.exception.NotFoundException;
import teamfive.exception.ServiceUnavailableException;

@Component
@Slf4j
public class EventServiceClientFallbackFactory implements FallbackFactory<EventServiceClient> {

    @Override
    public EventServiceClient create(Throwable cause) {
        return new EventServiceClient() {
            @Override
            public void incrementConfirmedRequests(Long eventId, Integer count) {
                log.error("[Fallback] Ошибка при обновлении счетчика: {}", cause.getMessage());
                // Если это 404 из декодера — пробрасываем её "как есть"
                if (cause instanceof NotFoundException) {
                    throw (NotFoundException) cause;
                }
                throw new ServiceUnavailableException("Сервис событий недоступен");
            }

            @Override
            public EventInternalDto getEventInternalById(Long eventId) {
                log.error("[Fallback] Ошибка при получении события {}: {}", eventId, cause.getMessage());
                // Проверяем: если ошибка — это наша 404, не подменяем её на 503
                if (cause instanceof NotFoundException) {
                    throw (NotFoundException) cause;
                }
                throw new ServiceUnavailableException("Сервис событий временно недоступен");
            }
        };
    }
}

