package com.reactivepractice.exception.hadler;

import com.reactivepractice.exception.model.CustomBaseException;
import com.reactivepractice.exception.model.ErrorCode;
import com.reactivepractice.exception.model.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class CommonsExceptionHandler {

    @ExceptionHandler(CustomBaseException.class)
    public ResponseEntity<Mono<ErrorResponse>> handle(CustomBaseException e){
        log.error("Exception", e);
        return createErrorResponseEntity(e.getErrorCode());
    }

    private ResponseEntity<Mono<ErrorResponse>> createErrorResponseEntity(ErrorCode errorCode) {
        return new ResponseEntity<>(Mono.just(ErrorResponse.of(errorCode)), errorCode.getHttpStatus());
    }
}
