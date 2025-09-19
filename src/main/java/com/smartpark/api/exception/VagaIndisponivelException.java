package com.smartpark.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Retorna HTTP 400
public class VagaIndisponivelException extends RuntimeException {
    public VagaIndisponivelException(String message) {
        super(message);
    }
}