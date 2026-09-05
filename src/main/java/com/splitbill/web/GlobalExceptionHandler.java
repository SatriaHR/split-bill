package com.splitbill.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> api(ApiException ex, HttpServletRequest request) { return response(ex.getStatus(), ex.getMessage(), request); }
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<?> validation(Exception ex, HttpServletRequest request) { return response(HttpStatus.BAD_REQUEST, "Request validation failed", request); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<?> unexpected(Exception ex, HttpServletRequest request) { return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request); }
    private ResponseEntity<?> response(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseHandler.response(status, message, Map.of("path", request.getRequestURI()));
    }
}
