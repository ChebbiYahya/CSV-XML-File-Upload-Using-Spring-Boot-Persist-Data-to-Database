package com.bank.uploadfileanddatapersistdb.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<ApiError> handleInvalidFileFormat(
            InvalidFileFormatException ex, HttpServletRequest request) {

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid file format",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ApiError> handleFileProcessing(
            FileProcessingException ex, HttpServletRequest request) {

        ApiError apiError = ApiError.of(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "File processing error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex, HttpServletRequest request) {

        ApiError apiError = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
