package com.reactivepractice.exception.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND),
    NOT_FOUND_USER("NOT_FOUND_USER", HttpStatus.NOT_FOUND),
    NOT_FOUND_POST("NOT_FOUND_POST", HttpStatus.NOT_FOUND),
    NOT_FOUND_COMMENT("NOT_FOUND_COMMENT", HttpStatus.NOT_FOUND),
    ALREADY_EXIST("ALREADY_EXIST", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;

    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
