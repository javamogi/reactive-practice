package com.reactivepractice.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ErrorResponse {
    private String error;
    private Integer statusCode;

    public ErrorResponse(ErrorCode code){
        this.error = code.getMessage();
        this.statusCode = code.getHttpStatus().value();
    }

    public ErrorResponse(ErrorCode code, String message){
        this.error = message;
        this.statusCode = code.getHttpStatus().value();
    }

    public static ErrorResponse of(ErrorCode code){
        return new ErrorResponse(code);
    }

    public static ErrorResponse of(ErrorCode code, String message){
        return new ErrorResponse(code, message);
    }
}
