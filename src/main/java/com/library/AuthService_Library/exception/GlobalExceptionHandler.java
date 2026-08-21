package com.library.AuthService_Library.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.library.AuthService_Library.data.dto.ApiErrorResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BadCredentialsException.class,
            AuthenticationServiceException.class,
            AuthenticationException.class,
            ExpiredJwtException.class,
            MalformedJwtException.class,
            SignatureException.class
    })
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            Exception exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", authenticationMessage(exception), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.FORBIDDEN, "Forbidden", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Request validation failed", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Request body is invalid", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request);
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(
            HttpStatus status, String error, String message, HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI().toString());
        return ResponseEntity.status(status).body(response);
    }

    private String authenticationMessage(Exception exception) {
        if (exception instanceof ExpiredJwtException) {
            return "Token has expired";
        }
        if (exception instanceof MalformedJwtException || exception instanceof SignatureException) {
            return "Token is invalid";
        }
        return "Authentication failed";
    }
}