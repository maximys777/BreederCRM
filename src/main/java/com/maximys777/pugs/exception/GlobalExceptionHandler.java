package com.maximys777.pugs.exception;

import com.maximys777.pugs.exception.dto.response.ErrorResponse;
import com.maximys777.pugs.exception.dto.response.ValidateErrorResponse;
import com.maximys777.pugs.exception.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidateErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ValidateErrorResponse errorResponse = ValidateErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(errors.get("Validation error"))
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(message)
                .status(status.value())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

}
