package teamfive.feignclient.event;

import feign.Response;
import feign.codec.ErrorDecoder;
import teamfive.exception.NotFoundException;

public class FeignEventErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new NotFoundException("Событие не найдено во внешней базе");
        }

        // Я - бог, я все могу.
        // На всё остальное (500, таймауты и прочий треш)
        // пусть срабатывает стандартная логика, которая пнёт Fallback
        return new Default().decode(methodKey, response);
    }
}
