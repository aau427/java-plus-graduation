package teamfive.feignclient.event;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Конфигурация для Feign-клиента сервиса событий.
 * Класс НЕ помечен @Configuration, чтобы настройки были изолированными.
 */
public class FeignEventClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignEventErrorDecoder();
    }
}
