package com.splitbill.web;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.splitbill.web.dto.Responses;

public final class ResponseHandler {
    private ResponseHandler() { }

    public static <T> Responses.ApiResponse<T> body(HttpStatus status, String message, T data) {
        return new Responses.ApiResponse<>(Instant.now(), status.value(), message, data);
    }

    public static <T> ResponseEntity<Responses.ApiResponse<T>> response(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status.value()).body(body(status, message, data));
    }
}