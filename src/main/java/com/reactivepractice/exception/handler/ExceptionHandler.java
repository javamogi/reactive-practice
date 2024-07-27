package com.reactivepractice.exception.handler;

import com.reactivepractice.exception.model.CustomBaseException;
import com.reactivepractice.exception.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ExceptionHandler {

    public Mono<ServerResponse> handleGlobalException(CustomBaseException ex) {
        log.error("Global exception", ex);
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode());

        return ServerResponse.status(ex.getErrorCode().getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }
}
