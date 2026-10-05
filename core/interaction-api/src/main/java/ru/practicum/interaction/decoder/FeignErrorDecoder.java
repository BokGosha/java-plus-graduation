package ru.practicum.interaction.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import ru.practicum.exception.NotFoundException;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == HttpStatus.NOT_FOUND.value()) {
            return new NotFoundException("Resource not found: " + methodKey);
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }
}
